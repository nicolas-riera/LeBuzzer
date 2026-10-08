import { Navigate, useParams } from "react-router";
import Header from "../../components/Header";
import { useGame } from "../../context/GameContext";
import "../../styles/WaitingQuizz.css";

export default function WaitingQuizz() {
    const { gameCode = "" } = useParams();
    const { session, snapshot, closedGameCode } = useGame();
    const code = gameCode.toUpperCase();

    if (closedGameCode === code) {
        return (
            <Navigate
                to="/JoinQuizz"
                replace
                state={{ error: "The host has left: the room is closed." }}
            />
        );
    }

    if (!session || session.gameCode !== code) {
        return <Navigate to={`/JoinQuizz/${gameCode}`} replace />;
    }

    const playerCount = snapshot?.onlinePlayers.length ?? 0;
    const hostConnected = snapshot?.hostConnected ?? true;

    return (
        <main className="waiting">
            <Header />

            <div className="waiting-content">
                <h1 className="page-title">You're in!</h1>

                <div className="waiting-card">
                    <span className="waiting-nickname">{session.nickname}</span>
                    <span className="waiting-room">
                        Room {session.gameCode}
                    </span>
                </div>

                <p className="waiting-status" aria-live="polite">
                    {hostConnected
                        ? "Waiting for the host to start"
                        : "The host is disconnected, waiting for them to come back"}
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
