import { useState } from "react";
import AnswerGrid from "../../components/AnswerGrid";
import Header from "../../components/Header";
import QuestionHeading from "../../components/QuestionHeading";
import Timer from "../../components/Timer";
import { useGame } from "../../context/GameContext";
import { vibrateBuzz } from "../../utils/haptics";
import "../../styles/Play.css";
import "../../styles/QuestionUser.css";

interface Selection {
    questionNumber: number;
    indices: number[];
}

export default function QuestionUser() {
    const { session, snapshot, answer, submitAnswer } = useGame();
    const [selection, setSelection] = useState<Selection | null>(null);
    const [buzzedQuestion, setBuzzedQuestion] = useState<number | null>(null);
    const question = snapshot?.currentQuestion;

    if (!session || !snapshot || !question) return null;

    const submitted =
        answer?.questionNumber === question.number ? answer : null;
    const selected =
        selection?.questionNumber === question.number ? selection.indices : [];
    const shown = submitted ? submitted.selectedIndices : selected;
    const buzzed = buzzedQuestion === question.number;

    function toggle(index: number) {
        if (!question) return;
        const indices = question.multipleChoice
            ? selected.includes(index)
                ? selected.filter((value) => value !== index)
                : [...selected, index]
            : [index];
        setSelection({ questionNumber: question.number, indices });
    }

    function buzz() {
        if (!question || selected.length === 0 || submitted) return;
        submitAnswer(selected);
        setBuzzedQuestion(question.number);
        vibrateBuzz();
    }

    const status = submitted
        ? "Answer sent! Waiting for the others…"
        : selected.length === 0
          ? question.multipleChoice
              ? "Pick one or more answers, then buzz!"
              : "Pick an answer, then buzz!"
          : "Buzz to lock in your answer!";

    return (
        <main
            className={
                buzzed ? "page page-narrow page-buzzed" : "page page-narrow"
            }
        >
            <Header gameCode={session.gameCode}>
                <Timer snapshot={snapshot} />
            </Header>

            <div className="page-content">
                <QuestionHeading question={question} />

                <AnswerGrid
                    options={question.options}
                    selected={shown}
                    disabled={submitted !== null}
                    onToggle={toggle}
                />

                <div className="play-footer">
                    <button
                        type="button"
                        className={[
                            "buzzer",
                            submitted && "buzzer-pressed",
                            buzzed && "buzzer-impact",
                        ]
                            .filter(Boolean)
                            .join(" ")}
                        onClick={buzz}
                        disabled={submitted !== null || selected.length === 0}
                        aria-label="Buzz to send your answer"
                    >
                        <img
                            className="buzzer-image"
                            src="/buzzer.webp"
                            alt=""
                            draggable={false}
                        />
                    </button>
                    <p className="play-status" aria-live="polite">
                        {status}
                    </p>
                </div>
            </div>

            {buzzed && <div className="buzz-flash" aria-hidden="true" />}
        </main>
    );
}
