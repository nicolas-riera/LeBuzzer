package com.nicolasgabriel.lebuzzer.service;

import static com.nicolasgabriel.lebuzzer.service.GameUtils.currentQuestion;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.dto.LeaderboardEntry;
import com.nicolasgabriel.lebuzzer.dto.QuestionView;
import com.nicolasgabriel.lebuzzer.dto.SubmittedAnswer;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.Question;

@Service
public class BuzzerService {
    private static final Duration DEFAULT_HOST_GRACE_PERIOD = Duration.ofSeconds(15);

    private final GameRegistry registry = new GameRegistry();
    private final ScoringService scoring = new ScoringService();
    private final GameViewFactory views = new GameViewFactory(scoring);
    private final PlayerService players = new PlayerService(registry);
    private final HostService hosts;
    private final QuestionFlowService questionFlow;

    private volatile Consumer<String> questionExpiredListener = code -> {
    };
    private volatile Consumer<String> gameClosedListener = code -> {
    };

    public BuzzerService() {
        this(DEFAULT_HOST_GRACE_PERIOD);
    }

    public BuzzerService(Duration hostGracePeriod) {
        GameSchedulerService scheduler = new GameSchedulerService();
        this.hosts = new HostService(registry, scheduler, hostGracePeriod,
                code -> gameClosedListener.accept(code));
        this.questionFlow = new QuestionFlowService(registry, scheduler, scoring,
                code -> questionExpiredListener.accept(code));
    }

    public void setQuestionExpiredListener(Consumer<String> listener) {
        this.questionExpiredListener = listener;
    }

    public void setGameClosedListener(Consumer<String> listener) {
        this.gameClosedListener = listener;
    }

    public Game createGame() {
        return hosts.createGame();
    }

    public void connectHost(String gameCode, String hostToken, String sessionId) {
        hosts.connectHost(gameCode, hostToken, sessionId);
    }

    public Question startNextQuestion(String gameCode, String hostToken) {
        return questionFlow.startNextQuestion(gameCode, hostToken);
    }

    public void closeQuestion(String gameCode, String hostToken) {
        questionFlow.closeQuestion(gameCode, hostToken);
    }

    public void showLeaderboard(String gameCode, String hostToken) {
        questionFlow.showLeaderboard(gameCode, hostToken);
    }

    public void finishGame(String gameCode, String hostToken) {
        questionFlow.finishGame(gameCode, hostToken);
    }

    public Player joinGame(String gameCode, String nickname, String token, String sessionId) {
        return players.joinGame(gameCode, nickname, token, sessionId);
    }

    public boolean submitAnswer(String gameCode, String sessionId, List<Integer> selectedIndices) {
        return players.submitAnswer(gameCode, sessionId, selectedIndices);
    }

    public Optional<SubmittedAnswer> getPlayerAnswer(String gameCode, String sessionId) {
        return players.getPlayerAnswer(gameCode, sessionId);
    }

    public Optional<String> disconnect(String sessionId) {
        String hostedGameCode = registry.unbindHostSession(sessionId);
        if (hostedGameCode != null) {
            return hosts.disconnectHost(hostedGameCode, sessionId);
        }
        return players.disconnect(sessionId);
    }

    public GameSnapshot getSnapshot(String gameCode) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            return views.snapshot(game);
        }
    }

    public List<LeaderboardEntry> getLeaderboard(String gameCode) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            return scoring.buildLeaderboard(game);
        }
    }

    public QuestionView getCurrentQuestionView(String gameCode) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            return views.questionView(game, currentQuestion(game));
        }
    }

    public Optional<Game> findGame(String gameCode) {
        return registry.find(gameCode);
    }
}
