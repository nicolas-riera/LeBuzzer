import type { LeaderboardEntry } from "../types/game";
import "../styles/Leaderboard.css";

export default function Leaderboard({
    entries,
}: {
    entries: LeaderboardEntry[];
}) {
    const podium = entries.slice(0, 3);
    const others = entries.slice(3);

    if (podium.length === 0) {
        return <p className="leaderboard-empty">No players took part.</p>;
    }

    return (
        <>
            <ol className="podium">
                {podium.map((entry, index) => (
                    <li
                        key={entry.playerId}
                        className={`podium-step podium-place-${index + 1}`}
                    >
                        <span className="podium-name">{entry.playerId}</span>
                        <span className="podium-score">{entry.score} pts</span>
                        <span className="podium-block">{entry.rank}</span>
                    </li>
                ))}
            </ol>

            {others.length > 0 && (
                <ol className="ranking-list">
                    {others.map((entry) => (
                        <li key={entry.playerId} className="ranking-row">
                            <span className="ranking-rank">{entry.rank}</span>
                            <span className="ranking-name">
                                {entry.playerId}
                            </span>
                            <span className="ranking-score">
                                {entry.score} pts
                            </span>
                        </li>
                    ))}
                </ol>
            )}
        </>
    );
}
