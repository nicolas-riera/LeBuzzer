import "../styles/AnswerGrid.css";

const TILE_COLORS = ["blue", "green", "red", "yellow"];

function CheckIcon() {
    return (
        <svg className="answer-mark" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 12.5l5 5L20 6.5" />
        </svg>
    );
}

function CrossIcon() {
    return (
        <svg className="answer-mark" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 6l12 12M18 6L6 18" />
        </svg>
    );
}

interface AnswerGridProps {
    options: string[];
    selected?: number[];
    correct?: number[] | null;
    disabled?: boolean;
    onToggle?: (index: number) => void;
}

export default function AnswerGrid({
    options,
    selected = [],
    correct = null,
    disabled = false,
    onToggle,
}: AnswerGridProps) {
    return (
        <ul
            className={
                disabled ? "answer-grid answer-grid-locked" : "answer-grid"
            }
        >
            {options.map((option, index) => {
                const isSelected = selected.includes(index);
                const isCorrect = correct?.includes(index) ?? false;
                const isWrong = correct !== null && !isCorrect;
                const className = [
                    "answer-tile",
                    `answer-tile-${TILE_COLORS[index]}`,
                    isSelected && "answer-tile-selected",
                    isWrong && "answer-tile-wrong",
                ]
                    .filter(Boolean)
                    .join(" ");

                const mark =
                    correct !== null ? (
                        isCorrect ? (
                            <CheckIcon />
                        ) : (
                            <CrossIcon />
                        )
                    ) : (
                        isSelected && <CheckIcon />
                    );

                return (
                    <li key={index}>
                        {onToggle ? (
                            <button
                                type="button"
                                className={className}
                                aria-pressed={isSelected}
                                disabled={disabled}
                                onClick={() => onToggle(index)}
                            >
                                <span>{option}</span>
                                {mark}
                            </button>
                        ) : (
                            <div className={className}>
                                <span>{option}</span>
                                {mark}
                            </div>
                        )}
                    </li>
                );
            })}
        </ul>
    );
}
