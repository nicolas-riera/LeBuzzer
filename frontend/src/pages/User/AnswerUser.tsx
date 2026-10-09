import Header from "../../components/Header";
import QuestionHeading from "../../components/QuestionHeading";
import { useGame } from "../../context/GameContext";
import type { GameSnapshot, SubmittedAnswer } from "../../types/game";
import { ordinal } from "../../utils/ordinal";
import "../../styles/Play.css";
import "../../styles/AnswerUser.css";

function sameAnswers(first: number[], second: number[]) {
    return (
        first.length === second.length &&
        first.every((index) => second.includes(index))
    );
}

function resultMessage(
    snapshot: GameSnapshot,
    answer: SubmittedAnswer | null,
    joinedAtQuestion: number | null,
    score: number,
) {
    const question = snapshot.currentQuestion;
    const correct = snapshot.correctAnswerIndices ?? [];
    if (!question) return null;

    if (!answer || answer.questionNumber !== question.number) {
        return {
            success: false,
            text:
                joinedAtQuestion === question.number
                    ? "You joined during this question. Get ready for the next one!"
                    : "Time's up! You didn't answer this question.",
        };
    }

    if (sameAnswers(answer.selectedIndices, correct)) {
        const earned = score - answer.scoreBefore;
        return {
            success: true,
            text: `Correct answer — well done! You've earned ${earned} points.`,
        };
    }

    const rightAnswers = correct
        .map((index) => question.options[index])
        .join(" & ");
    return {
        success: false,
        text: `Wrong answer… The right answer was ${rightAnswers}.`,
    };
}

export default function AnswerUser() {
    const { session, snapshot, answer, joinedAtQuestion } = useGame();

    if (!session || !snapshot) return null;

    const ranking = snapshot.leaderboard.find(
        (entry) => entry.playerId === session.nickname,
    );
    const score = ranking?.score ?? 0;
    const playerCount = snapshot.leaderboard.length;
    const result = resultMessage(snapshot, answer, joinedAtQuestion, score);

    return (
        <main className="page page-narrow">
            <Header gameCode={session.gameCode} />

            <div className="page-content">
                {snapshot.currentQuestion && (
                    <QuestionHeading
                        question={snapshot.currentQuestion}
                        showText={false}
                    />
                )}

                <div className="result">
                    {result && (
                        <p
                            className={
                                result.success
                                    ? "result-message result-success"
                                    : "result-message"
                            }
                        >
                            {result.text}
                        </p>
                    )}
                    <dl className="result-stats">
                        <div className="result-stat card">
                            <dt className="label">Score</dt>
                            <dd>
                                {score} {score === 1 ? "point" : "points"}
                            </dd>
                        </div>
                        {ranking && (
                            <div className="result-stat card">
                                <dt className="label">Ranking</dt>
                                <dd>
                                    {ordinal(ranking.rank)}
                                    <span className="result-stat-detail">
                                        {" "}
                                        / {playerCount}
                                    </span>
                                </dd>
                            </div>
                        )}
                    </dl>
                </div>

                <p className="result-waiting" aria-live="polite">
                    Waiting for the host
                    <span className="waiting-dots" aria-hidden="true">
                        <span>.</span>
                        <span>.</span>
                        <span>.</span>
                    </span>
                </p>
            </div>
        </main>
    );
}
