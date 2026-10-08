import { Link } from "react-router";
import AnswerGrid from "../../components/AnswerGrid";
import Header from "../../components/Header";
import QuestionHeading from "../../components/QuestionHeading";
import { useHost } from "../../context/HostContext";
import "../../styles/Play.css";

export default function AnswerHost() {
    const { snapshot, connected, startNextQuestion, finishGame } = useHost();

    if (!snapshot) return null;

    const question = snapshot.currentQuestion;

    if (snapshot.state === "FINISHED" || !question) {
        return (
            <main className="play">
                <Header />

                <div className="play-content">
                    <h1 className="page-title">The game is over!</h1>

                    <div className="play-host-footer">
                        <Link to="/CreateQuizz" className="button">
                            New game
                        </Link>
                    </div>
                </div>
            </main>
        );
    }

    const lastQuestion = question.number >= question.totalQuestions;

    return (
        <main className="play">
            <Header />

            <div className="play-content">
                <QuestionHeading question={question} />

                <AnswerGrid
                    options={question.options}
                    correct={snapshot.correctAnswerIndices ?? []}
                />

                <div className="play-host-footer">
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
