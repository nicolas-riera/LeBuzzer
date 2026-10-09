import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import { Navigate, useParams } from "react-router";
import ConnectionNotice from "./ConnectionNotice";
import ConnectionStatus from "./ConnectionStatus";
import { useGame } from "../context/GameContext";
import { usePlayerNavigation } from "../hooks/useGameNavigation";
import { loadPlayerSession } from "../services/gameApi";

export default function RequirePlayer({ children }: { children: ReactNode }) {
    const { gameCode = "" } = useParams();
    const { session, snapshot, connected, closedGameCode, join } = useGame();
    const code = gameCode.toUpperCase();
    const inRoom = session?.gameCode === code;
    const [stored] = useState(() => loadPlayerSession());
    const [resumeFailed, setResumeFailed] = useState(false);
    const resumeRequested = useRef(false);
    const canResume = !inRoom && stored?.gameCode === code;

    usePlayerNavigation(code, inRoom ? snapshot?.state : undefined);

    useEffect(() => {
        if (!canResume || !stored || resumeRequested.current) return;
        resumeRequested.current = true;
        join(code, stored.nickname).catch(() => setResumeFailed(true));
    }, [canResume, stored, code, join]);

    if (closedGameCode === code) {
        return (
            <Navigate
                to="/JoinQuiz"
                replace
                state={{
                    error: "The host was away for too long: the room has been closed.",
                }}
            />
        );
    }

    if (!inRoom) {
        if (canResume && !resumeFailed) {
            return <ConnectionNotice message="Reconnecting to your game…" />;
        }
        return <Navigate to={`/JoinQuiz/${gameCode}`} replace />;
    }

    const finished = snapshot?.state === "FINISHED";

    return (
        <>
            {children}
            {!finished && !connected && (
                <ConnectionNotice message="Connection lost, reconnecting…" />
            )}
            {!finished && connected && snapshot && !snapshot.hostConnected && (
                <ConnectionNotice message="The host is disconnected. The room will close if they don't come back." />
            )}
            {!finished && <ConnectionStatus connected={connected} />}
        </>
    );
}
