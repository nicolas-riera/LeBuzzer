import Header from "../../components/Header";
import { useGame } from "../../context/GameContext";
import "../../styles/WaitingQuiz.css";

export default function WaitingQuiz() {
    const { session, snapshot } = useGame();

    if (!session) return null;

    const playerCount = snapshot?.onlinePlayers.length ?? 0;

    return (
        <main className="page">
            <Header gameCode={session.gameCode} />

            <div className="page-content waiting-content">
                <h1 className="page-title">You're in!</h1>

                <div className="waiting-card card">
                    <span className="label">Playing as</span>
                    <span className="waiting-nickname">{session.nickname}</span>
                </div>

                <p className="waiting-status" aria-live="polite">
                    Waiting for the host to start
                    <span className="waiting-dots" aria-hidden="true">
                        <span>.</span>
                        <span>.</span>
                        <span>.</span>
                    </span>
                </p>

                <p className="waiting-count">
                    {playerCount} {playerCount === 1 ? "player" : "players"}{" "}
                    online
                </p>
            </div>
        </main>
    );
}
