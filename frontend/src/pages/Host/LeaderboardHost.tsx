import EndGameButton from "../../components/EndGameButton";
import Header from "../../components/Header";
import Leaderboard from "../../components/Leaderboard";
import { useHost } from "../../context/HostContext";
import "../../styles/Play.css";

export default function LeaderboardHost() {
    const { snapshot, connected, startNextQuestion, finishGame } = useHost();
    const question = snapshot?.currentQuestion;

    if (!snapshot || !question) return null;

    const lastQuestion = question.number >= question.totalQuestions;

    return (
        <main className="page page-narrow">
            <Header />

            <div className="page-content">
                <div className="play-heading">
                    <h1 className="page-title">Leaderboard</h1>
                    <p className="play-subtitle">
                        After question {question.number} of{" "}
                        {question.totalQuestions}
                    </p>
                </div>

                <Leaderboard entries={snapshot.leaderboard} />

                <div className="page-actions">
                    {!lastQuestion && <EndGameButton />}
                    <button
                        type="button"
                        className="button"
                        onClick={lastQuestion ? finishGame : startNextQuestion}
                        disabled={!connected}
                    >
                        {lastQuestion ? "Finish" : "Next"}
                    </button>
                </div>
            </div>
        </main>
    );
}
