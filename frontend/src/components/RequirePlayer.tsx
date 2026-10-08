import type { ReactNode } from "react";
import { Navigate, useParams } from "react-router";
import ConnectionNotice from "./ConnectionNotice";
import { useGame } from "../context/GameContext";
import { usePlayerNavigation } from "../hooks/useGameNavigation";

export default function RequirePlayer({ children }: { children: ReactNode }) {
    const { gameCode = "" } = useParams();
    const { session, snapshot, closedGameCode } = useGame();
    const code = gameCode.toUpperCase();
    const inRoom = session?.gameCode === code;

    usePlayerNavigation(code, inRoom ? snapshot?.state : undefined);

    if (closedGameCode === code) {
        return (
            <Navigate
                to="/JoinQuizz"
                replace
                state={{
                    error: "The host was away for too long: the room has been closed.",
                }}
            />
        );
    }

    if (!inRoom) {
        return <Navigate to={`/JoinQuizz/${gameCode}`} replace />;
    }

    return (
        <>
            {children}
            {snapshot &&
                !snapshot.hostConnected &&
                snapshot.state !== "FINISHED" && (
                    <ConnectionNotice message="The host is disconnected. The room will close if they don't come back." />
                )}
        </>
    );
}
