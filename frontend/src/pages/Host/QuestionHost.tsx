import AnswerGrid from "../../components/AnswerGrid";
import Header from "../../components/Header";
import QuestionHeading from "../../components/QuestionHeading";
import Timer from "../../components/Timer";
import { useHost } from "../../context/HostContext";
import "../../styles/Play.css";

export default function QuestionHost() {
    const { snapshot, connected, closeQuestion } = useHost();
    const question = snapshot?.currentQuestion;

    if (!snapshot || !question) return null;

    return (
        <main className="play">
            <Header>
                <Timer snapshot={snapshot} />
            </Header>

            <div className="play-content">
                <QuestionHeading question={question} />

                <AnswerGrid options={question.options} />

                <div className="play-host-footer">
                    <p className="play-answered" aria-live="polite">
                        {snapshot.answeredCount}/{snapshot.onlinePlayers.length}
                        <span className="visually-hidden">
                            {" "}
                            players answered
                        </span>
                    </p>
                    <button
                        type="button"
                        className="button"
                        onClick={closeQuestion}
                        disabled={!connected}
                    >
                        End
                    </button>
                </div>
            </div>
        </main>
    );
}
