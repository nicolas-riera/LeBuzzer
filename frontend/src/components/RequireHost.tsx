import { useEffect, useRef } from "react";
import type { ReactNode } from "react";
import { Navigate } from "react-router";
import ConnectionNotice from "./ConnectionNotice";
import { useHost } from "../context/HostContext";
import { useHostNavigation } from "../hooks/useGameNavigation";

export default function RequireHost({ children }: { children: ReactNode }) {
    const { game, snapshot, connected, error, resumeRoom } = useHost();

    useHostNavigation(snapshot?.state);

    const resumeRequested = useRef(false);

    useEffect(() => {
        if (game || resumeRequested.current) return;
        resumeRequested.current = true;
        resumeRoom();
    }, [game, resumeRoom]);

    if (error) {
        return <Navigate to="/CreateQuizz" replace />;
    }

    if (!game || !snapshot) {
        return null;
    }

    return (
        <>
            {children}
            {!connected && (
                <ConnectionNotice message="Connection lost, reconnecting… The room closes if you stay away too long." />
            )}
        </>
    );
}
