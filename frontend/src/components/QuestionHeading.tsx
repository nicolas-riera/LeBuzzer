import type { QuestionView } from "../types/game";
import { ordinal } from "../utils/ordinal";
import "../styles/QuestionHeading.css";

interface QuestionHeadingProps {
    question: QuestionView;
    showText?: boolean;
}

export default function QuestionHeading({
    question,
    showText = true,
}: QuestionHeadingProps) {
    return (
        <div className="question-heading">
            <h1 className="question-number">
                {ordinal(question.number)} question
            </h1>
            {showText && (
                <>
                    <p className="question-type">
                        {question.multipleChoice
                            ? "multiple-choice question"
                            : "single-choice question"}
                    </p>
                    <p className="question-text">{question.text}</p>
                </>
            )}
        </div>
    );
}
