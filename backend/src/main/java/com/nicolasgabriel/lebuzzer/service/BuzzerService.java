package com.nicolasgabriel.lebuzzer.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.dto.QuestionView;
import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.enums.PlayerStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.PlayerAnswer;
import com.nicolasgabriel.lebuzzer.model.Question;
import com.nicolasgabriel.lebuzzer.model.QuestionCatalog;

@Service
public class BuzzerService {

    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 5;
    private static final int QUESTIONS_PER_GAME = 10;

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, String> gameCodeBySessionId = new ConcurrentHashMap<>();

    private final ScoringService scoringService;
    private final GameSchedulerService schedulerService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SecureRandom random = new SecureRandom();

    public BuzzerService(ScoringService scoringService,
                         GameSchedulerService schedulerService,
                         SimpMessagingTemplate messagingTemplate) {
        this.scoringService = scoringService;
        this.schedulerService = schedulerService;
        this.messagingTemplate = messagingTemplate;
    }

    public Game createGame() {
        List<Question> questions = new ArrayList<>(QuestionCatalog.getAllQuestions());
        Collections.shuffle(questions, random);
        questions = new ArrayList<>(questions.subList(0, Math.min(QUESTIONS_PER_GAME, questions.size())));

        Game game;
        do {
            game = new Game(generateCode(), questions);
        } while (games.putIfAbsent(game.getGameCode(), game) != null);

        game.setHostToken(UUID.randomUUID().toString());
        return game;
    }

    public Question startNextQuestion(String gameCode, String hostToken) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            GameStates state = game.getCurrentState();
            if (state == GameStates.QUIZZING || state == GameStates.FINISHED) {
                throw new IllegalStateException("Cannot start a question while game is " + state);
            }
            int nextIndex = game.getCurrentQuestionIndex() + 1;
            if (nextIndex >= game.getQuestionList().size()) {
                throw new IllegalStateException("No more questions");
            }
            game.setCurrentQuestionIndex(nextIndex);
            game.getCurrentAnswers().clear();
            game.setQuestionStartTime(LocalDateTime.now());
            game.setCurrentState(GameStates.QUIZZING);

            Question question = game.getQuestionList().get(nextIndex);
            schedulerService.scheduleQuestionExpiration(gameCode, question.getDurationInSeconds(),
                    () -> onQuestionExpired(gameCode, nextIndex));

            notifyGameUpdate(gameCode);
            return question;
        }
    }

    public void closeQuestion(String gameCode, String hostToken) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (!closeQuestionLocked(game)) {
                throw new IllegalStateException("No question in progress");
            }
            notifyGameUpdate(gameCode);
        }
    }

    public Player joinGame(String gameCode, String nickname, String sessionId) {
        Game game = requireGame(gameCode);
        String cleanNickname = nickname == null ? "" : nickname.trim();
        if (cleanNickname.isEmpty() || cleanNickname.length() > 20) {
            throw new IllegalArgumentException("Nickname must be between 1 and 20 characters");
        }
        synchronized (game) {
            if (game.getCurrentState() == GameStates.FINISHED) {
                throw new IllegalStateException("Game is finished");
            }
            Optional<Player> existing = findPlayerByNickname(game, cleanNickname);
            Player player;
            if (existing.isPresent()) {
                player = existing.get();
                if (player.getPlayerStatus() == PlayerStates.ONLINE) {
                    throw new IllegalStateException("Nickname already taken");
                }
                gameCodeBySessionId.remove(player.getSessionId());
                player.setSessionId(sessionId);
                player.setPlayerStatus(PlayerStates.ONLINE);
            } else {
                player = new Player(cleanNickname, sessionId);
                game.getPlayerList().add(player);
            }
            gameCodeBySessionId.put(sessionId, game.getGameCode());
            notifyGameUpdate(gameCode);
            return player;
        }
    }

    public boolean submitAnswer(String gameCode, String sessionId, List<Integer> selectedIndices) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            Player player = findPlayerBySession(game, sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown player"));
            if (game.getCurrentState() != GameStates.QUIZZING) {
                return false;
            }
            Question question = currentQuestion(game);
            if (remainingMillis(game, question) <= 0) {
                return false;
            }
            if (game.getCurrentAnswers().containsKey(player.getId())) {
                return false;
            }
            List<Integer> indices = selectedIndices == null ? List.of() : selectedIndices;
            for (Integer index : indices) {
                if (index == null || index < 0 || index >= question.getOptions().size()) {
                    throw new IllegalArgumentException("Invalid option index");
                }
            }
            game.getCurrentAnswers().put(player.getId(),
                    new PlayerAnswer(player.getId(), question.getId(), new ArrayList<>(new HashSet<>(indices))));

            notifyGameUpdate(gameCode);
            return true;
        }
    }

    public Optional<String> disconnect(String sessionId) {
        String gameCode = gameCodeBySessionId.remove(sessionId);
        if (gameCode == null) {
            return Optional.empty();
        }
        Game game = games.get(gameCode);
        if (game == null) {
            return Optional.empty();
        }
        synchronized (game) {
            findPlayerBySession(game, sessionId).ifPresent(p -> p.setPlayerStatus(PlayerStates.OFFLINE));
            notifyGameUpdate(gameCode);
        }
        return Optional.of(gameCode);
    }

    public GameSnapshot getSnapshot(String gameCode) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            GameStates state = game.getCurrentState();
            boolean questionVisible = state == GameStates.QUIZZING || state == GameStates.QUIZ_RESULTS;
            Question question = questionVisible ? currentQuestion(game) : null;
            QuestionView view = question == null ? null : toView(question);
            long remaining = state == GameStates.QUIZZING ? Math.max(0, remainingMillis(game, question)) : 0;
            List<Integer> correct = state == GameStates.QUIZ_RESULTS ? question.getCorrectAnswerIndices() : null;
            return new GameSnapshot(
                    game.getGameCode(),
                    state,
                    onlineNicknames(game),
                    view,
                    remaining,
                    game.getCurrentAnswers().size(),
                    correct,
                    scoringService.buildLeaderboard(game));
        }
    }

    private void onQuestionExpired(String gameCode, int questionIndex) {
        Game game = games.get(gameCode);
        if (game == null) {
            return;
        }
        boolean closed;
        synchronized (game) {
            closed = game.getCurrentQuestionIndex() == questionIndex && closeQuestionLocked(game);
        }
        if (closed) {
            notifyGameUpdate(gameCode);
        }
    }

    private boolean closeQuestionLocked(Game game) {
        if (game.getCurrentState() != GameStates.QUIZZING) {
            return false;
        }
        schedulerService.cancelExpiration(game.getGameCode());
        Question question = currentQuestion(game);
        for (Player player : game.getPlayerList()) {
            PlayerAnswer answer = game.getCurrentAnswers().get(player.getId());
            if (answer != null) {
                player.setScore(player.getScore() + scoringService.computePoints(game, question, answer));
            }
        }
        game.setCurrentState(GameStates.QUIZ_RESULTS);
        return true;
    }

    private void notifyGameUpdate(String gameCode) {
        messagingTemplate.convertAndSend("/topic/game/" + gameCode, getSnapshot(gameCode));
    }

    private long remainingMillis(Game game, Question question) {
        long elapsed = Duration.between(game.getQuestionStartTime(), LocalDateTime.now()).toMillis();
        return question.getDurationInSeconds() * 1000L - elapsed;
    }

    private Question currentQuestion(Game game) {
        return game.getQuestionList().get(game.getCurrentQuestionIndex());
    }

    private QuestionView toView(Question question) {
        return new QuestionView(question.getId(), question.getText(),
                List.copyOf(question.getOptions()), question.getDurationInSeconds());
    }

    private List<String> onlineNicknames(Game game) {
        return game.getPlayerList().stream()
                .filter(p -> p.getPlayerStatus() == PlayerStates.ONLINE)
                .map(Player::getId)
                .toList();
    }

    private Optional<Player> findPlayerBySession(Game game, String sessionId) {
        return game.getPlayerList().stream().filter(p -> p.getSessionId().equals(sessionId)).findFirst();
    }

    private Optional<Player> findPlayerByNickname(Game game, String nickname) {
        return game.getPlayerList().stream().filter(p -> p.getId().equalsIgnoreCase(nickname)).findFirst();
    }

    private Game requireGame(String gameCode) {
        Game game = gameCode == null ? null : games.get(gameCode.toUpperCase());
        if (game == null) {
            throw new IllegalArgumentException("Unknown game code");
        }
        return game;
    }

    private void requireHost(Game game, String hostToken) {
        if (hostToken == null || !hostToken.equals(game.getHostToken())) {
            throw new SecurityException("Only the host can do this");
        }
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }
}