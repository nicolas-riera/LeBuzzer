import { Link } from "react-router";
import Header from "../../components/Header";
import { useGame } from "../../context/GameContext";
import { ordinal } from "../../utils/ordinal";
import "../../styles/GameOver.css";

export default function GameOverUser() {
    const { session, snapshot } = useGame();

    if (!session || !snapshot) return null;

    const ranking = snapshot.leaderboard.find(
        (entry) => entry.playerId === session.nickname,
    );
    const score = ranking?.score ?? 0;

    return (
        <main className="page page-narrow">
            <Header />

            <div className="page-content">
                <h1 className="page-title gameover-title">Quiz over</h1>

                <div className="gameover-result">
                    {ranking ? (
                        <p className="gameover-rank">
                            GG, you are
                            <strong>{ordinal(ranking.rank)}</strong>
                            <span className="gameover-rank-detail">
                                out of {snapshot.leaderboard.length}
                            </span>
                        </p>
                    ) : (
                        <p className="gameover-rank">GG!</p>
                    )}
                    <p className="gameover-score card">
                        You have {score} {score === 1 ? "point" : "points"}
                    </p>
                </div>

                <div className="page-actions">
                    <Link to="/" className="button">
                        Go Home
                    </Link>
                </div>
            </div>
        </main>
    );
}
