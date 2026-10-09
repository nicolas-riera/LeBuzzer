import AnswerGrid from "../../components/AnswerGrid";
import EndGameButton from "../../components/EndGameButton";
import Header from "../../components/Header";
import QuestionHeading from "../../components/QuestionHeading";
import { useHost } from "../../context/HostContext";
import "../../styles/Play.css";

export default function AnswerHost() {
    const { snapshot, connected, showLeaderboard } = useHost();
    const question = snapshot?.currentQuestion;

    if (!snapshot || !question) return null;

    return (
        <main className="page page-narrow">
            <Header />

            <div className="page-content">
                <QuestionHeading question={question} />

                <AnswerGrid
                    options={question.options}
                    correct={snapshot.correctAnswerIndices ?? []}
                />

                <div className="page-actions">
                    <EndGameButton />
                    <button
                        type="button"
                        className="button"
                        onClick={showLeaderboard}
                        disabled={!connected}
                    >
                        Leaderboard
                    </button>
                </div>
            </div>
        </main>
    );
}
