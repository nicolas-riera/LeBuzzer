package com.nicolasgabriel.lebuzzer.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.dto.LeaderboardEntry;
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
    private static final int BASE_POINTS = 500;
    private static final int MAX_SPEED_BONUS = 500;
    private static final Duration DEFAULT_HOST_GRACE_PERIOD = Duration.ofSeconds(15);
    private static final long ANSWER_GRACE_MILLIS = 500;

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, String> gameCodeBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> hostedGameCodeBySessionId = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> closingTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SecureRandom random = new SecureRandom();
    private final Duration hostGracePeriod;

    private volatile Consumer<String> questionExpiredListener = code -> {
    };
    private volatile Consumer<String> gameClosedListener = code -> {
    };

    public BuzzerService() {
        this(DEFAULT_HOST_GRACE_PERIOD);
    }

    public BuzzerService(Duration hostGracePeriod) {
        this.hostGracePeriod = hostGracePeriod;
    }

    public void setQuestionExpiredListener(Consumer<String> listener) {
        this.questionExpiredListener = listener;
    }

    public void setGameClosedListener(Consumer<String> listener) {
        this.gameClosedListener = listener;
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
        scheduleClosing(game.getGameCode());
        return game;
    }

    public void connectHost(String gameCode, String hostToken, String sessionId) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            String previousSessionId = game.getHostSessionId();
            if (previousSessionId != null) {
                hostedGameCodeBySessionId.remove(previousSessionId);
            }
            game.setHostSessionId(sessionId);
            hostedGameCodeBySessionId.put(sessionId, game.getGameCode());
            cancelClosing(game.getGameCode());
        }
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
            scheduleExpiration(game, question);
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
        }
    }

    public void showLeaderboard(String gameCode, String hostToken) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (game.getCurrentState() != GameStates.QUIZ_RESULTS) {
                throw new IllegalStateException("Leaderboard is available after a question is closed");
            }
            game.setCurrentState(GameStates.LEADERBOARD);
        }
    }

    public void finishGame(String gameCode, String hostToken) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (game.getCurrentState() == GameStates.QUIZZING) {
                closeQuestionLocked(game);
            }
            game.setCurrentState(GameStates.FINISHED);
        }
    }

    public Player joinGame(String gameCode, String nickname, String token, String sessionId) {
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
            boolean resuming = existing.isPresent() && token != null && token.equals(existing.get().getToken());
            if (!resuming && game.getHostSessionId() == null) {
                throw new IllegalStateException("The host is not connected");
            }
            Player player;
            if (existing.isPresent()) {
                player = existing.get();
                if (!resuming && (player.getPlayerStatus() == PlayerStates.ONLINE || player.getToken() != null)) {
                    throw new IllegalStateException("Nickname already taken");
                }
                gameCodeBySessionId.remove(player.getSessionId());
                player.setSessionId(sessionId);
                player.setPlayerStatus(PlayerStates.ONLINE);
            } else {
                player = new Player(cleanNickname, sessionId, token);
                game.getPlayerList().add(player);
            }
            gameCodeBySessionId.put(sessionId, game.getGameCode());
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
            if (remainingMillis(game, question) + ANSWER_GRACE_MILLIS <= 0) {
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
            return true;
        }
    }

    public Optional<String> disconnect(String sessionId) {
        String hostedGameCode = hostedGameCodeBySessionId.remove(sessionId);
        if (hostedGameCode != null) {
            return disconnectHost(hostedGameCode, sessionId);
        }

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
        }
        return Optional.of(gameCode);
    }

    public GameSnapshot getSnapshot(String gameCode) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            GameStates state = game.getCurrentState();
            boolean resultsVisible = state == GameStates.QUIZ_RESULTS || state == GameStates.LEADERBOARD;
            boolean questionVisible = state == GameStates.QUIZZING || resultsVisible;
            Question question = questionVisible ? currentQuestion(game) : null;
            QuestionView view = question == null ? null : toView(game, question);
            long remaining = state == GameStates.QUIZZING ? Math.max(0, remainingMillis(game, question)) : 0;
            List<Integer> correct = resultsVisible ? question.getCorrectAnswerIndices() : null;
            return new GameSnapshot(
                    game.getGameCode(),
                    state,
                    onlineNicknames(game),
                    view,
                    remaining,
                    game.getCurrentAnswers().size(),
                    correct,
                    buildLeaderboard(game),
                    game.getHostSessionId() != null,
                    game.nextSnapshotSequence());
        }
    }

    public List<LeaderboardEntry> getLeaderboard(String gameCode) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            return buildLeaderboard(game);
        }
    }

    public QuestionView getCurrentQuestionView(String gameCode) {
        Game game = requireGame(gameCode);
        synchronized (game) {
            return toView(game, currentQuestion(game));
        }
    }

    public Optional<Game> findGame(String gameCode) {
        return Optional.ofNullable(gameCode).map(games::get);
    }

    private Optional<String> disconnectHost(String gameCode, String sessionId) {
        Game game = games.get(gameCode);
        if (game == null) {
            return Optional.empty();
        }
        synchronized (game) {
            if (sessionId.equals(game.getHostSessionId())) {
                game.setHostSessionId(null);
                scheduleClosing(gameCode);
            }
        }
        return Optional.of(gameCode);
    }

    private void scheduleClosing(String gameCode) {
        cancelClosing(gameCode);
        ScheduledFuture<?> task = scheduler.schedule(() -> closeAbandonedGame(gameCode),
                hostGracePeriod.toMillis(), TimeUnit.MILLISECONDS);
        closingTasks.put(gameCode, task);
    }

    private void cancelClosing(String gameCode) {
        ScheduledFuture<?> task = closingTasks.remove(gameCode);
        if (task != null) {
            task.cancel(false);
        }
    }

    private void closeAbandonedGame(String gameCode) {
        Game game = games.get(gameCode);
        if (game == null) {
            return;
        }
        synchronized (game) {
            if (game.getHostSessionId() != null) {
                return;
            }
            games.remove(gameCode);
            closingTasks.remove(gameCode);
            cancelExpiration(gameCode);
            for (Player player : game.getPlayerList()) {
                gameCodeBySessionId.remove(player.getSessionId());
            }
        }
        gameClosedListener.accept(gameCode);
    }

    private void scheduleExpiration(Game game, Question question) {
        cancelExpiration(game.getGameCode());
        String code = game.getGameCode();
        int expectedIndex = game.getCurrentQuestionIndex();
        ScheduledFuture<?> task = scheduler.schedule(() -> onExpiration(code, expectedIndex),
                question.getDurationInSeconds() * 1000L + ANSWER_GRACE_MILLIS, TimeUnit.MILLISECONDS);
        expirationTasks.put(code, task);
    }

    private void onExpiration(String gameCode, int questionIndex) {
        Game game = games.get(gameCode);
        if (game == null) {
            return;
        }
        boolean closed;
        synchronized (game) {
            closed = game.getCurrentQuestionIndex() == questionIndex && closeQuestionLocked(game);
        }
        if (closed) {
            questionExpiredListener.accept(gameCode);
        }
    }

    private void cancelExpiration(String gameCode) {
        ScheduledFuture<?> task = expirationTasks.remove(gameCode);
        if (task != null) {
            task.cancel(false);
        }
    }

    private boolean closeQuestionLocked(Game game) {
        if (game.getCurrentState() != GameStates.QUIZZING) {
            return false;
        }
        cancelExpiration(game.getGameCode());
        Question question = currentQuestion(game);
        for (Player player : game.getPlayerList()) {
            PlayerAnswer answer = game.getCurrentAnswers().get(player.getId());
            if (answer != null) {
                player.setScore(player.getScore() + computePoints(game, question, answer));
            }
        }
        game.setCurrentState(GameStates.QUIZ_RESULTS);
        return true;
    }

    private int computePoints(Game game, Question question, PlayerAnswer answer) {
        if (!new HashSet<>(answer.getSelectedOptionIndices())
                .equals(new HashSet<>(question.getCorrectAnswerIndices()))) {
            return 0;
        }
        long durationMillis = question.getDurationInSeconds() * 1000L;
        long elapsed = Duration.between(game.getQuestionStartTime(), answer.getTimestamp()).toMillis();
        long remaining = Math.max(0, Math.min(durationMillis, durationMillis - elapsed));
        return BASE_POINTS + (int) (MAX_SPEED_BONUS * remaining / durationMillis);
    }

    private long remainingMillis(Game game, Question question) {
        long elapsed = Duration.between(game.getQuestionStartTime(), LocalDateTime.now()).toMillis();
        return question.getDurationInSeconds() * 1000L - elapsed;
    }

    private Question currentQuestion(Game game) {
        return game.getQuestionList().get(game.getCurrentQuestionIndex());
    }

    private QuestionView toView(Game game, Question question) {
        return new QuestionView(
                question.getId(),
                game.getCurrentQuestionIndex() + 1,
                game.getQuestionList().size(),
                question.getText(),
                List.copyOf(question.getOptions()),
                question.getCorrectAnswerIndices().size() > 1,
                question.getDurationInSeconds());
    }

    private List<String> onlineNicknames(Game game) {
        return game.getPlayerList().stream()
                .filter(p -> p.getPlayerStatus() == PlayerStates.ONLINE)
                .map(Player::getId)
                .toList();
    }

    private List<LeaderboardEntry> buildLeaderboard(Game game) {
        List<Player> sorted = game.getPlayerList().stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .toList();
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Player player = sorted.get(i);
            int rank = (i > 0 && sorted.get(i - 1).getScore() == player.getScore())
                    ? entries.get(i - 1).rank()
                    : i + 1;
            entries.add(new LeaderboardEntry(rank, player.getId(), player.getScore()));
        }
        return entries;
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
