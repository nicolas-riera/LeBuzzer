import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";
import type { ReactNode } from "react";
import type { Client } from "@stomp/stompjs";
import {
    clearHostGame,
    createGame,
    getSnapshot,
    loadHostGame,
    saveHostGame,
} from "../services/gameApi";
import {
    connectHost,
    connectToGame,
    sendHostAction,
} from "../services/gameSocket";
import type { HostAction } from "../services/gameSocket";
import type { GameSnapshot, HostGame } from "../types/game";
import { latestSnapshot } from "../utils/snapshot";

interface HostContextValue {
    game: HostGame | null;
    snapshot: GameSnapshot | null;
    connected: boolean;
    connectionLost: boolean;
    error: string | null;
    roomClosed: boolean;
    openRoom: () => Promise<void>;
    resumeRoom: () => Promise<void>;
    startNextQuestion: () => void;
    closeQuestion: () => void;
    showLeaderboard: () => void;
    finishGame: () => void;
    closeRoom: () => void;
}

const HostContext = createContext<HostContextValue | null>(null);

export function HostProvider({ children }: { children: ReactNode }) {
    const [game, setGame] = useState<HostGame | null>(null);
    const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null);
    const [connected, setConnected] = useState(false);
    const [connectionLost, setConnectionLost] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [roomClosed, setRoomClosed] = useState(false);
    const clientRef = useRef<Client | null>(null);
    const pendingRef = useRef<Promise<void> | null>(null);

    const loadRoom = useCallback(
        async (createIfMissing: boolean) => {
            setError(null);
            setRoomClosed(false);
            const saved = loadHostGame();
            if (saved && saved.gameCode === game?.gameCode) {
                if (!createIfMissing || snapshot?.state === "WAITING") return;
            }

            if (saved) {
                const current = await getSnapshot(saved.gameCode);
                if (
                    current &&
                    (!createIfMissing || current.state === "WAITING")
                ) {
                    setSnapshot(current);
                    setGame(saved);
                    return;
                }
                if (!current && !createIfMissing) {
                    clearHostGame();
                    setGame(null);
                    setSnapshot(null);
                    setRoomClosed(true);
                    setError("This room has been closed.");
                    return;
                }
            }

            if (!createIfMissing) {
                setGame(null);
                setSnapshot(null);
                setError("No room in progress.");
                return;
            }

            setGame(null);
            setSnapshot(null);
            const created = await createGame();
            saveHostGame(created);
            setSnapshot(await getSnapshot(created.gameCode));
            setGame(created);
        },
        [game, snapshot],
    );

    const runOnce = useCallback(
        (createIfMissing: boolean) => {
            if (!pendingRef.current) {
                pendingRef.current = loadRoom(createIfMissing)
                    .catch(() =>
                        setError("Unable to reach the server. Is it running?"),
                    )
                    .finally(() => {
                        pendingRef.current = null;
                    });
            }
            return pendingRef.current;
        },
        [loadRoom],
    );

    const openRoom = useCallback(() => runOnce(true), [runOnce]);
    const resumeRoom = useCallback(() => runOnce(false), [runOnce]);

    useEffect(() => {
        if (!game) return;

        let active = true;
        let wasConnected = false;

        const client = connectToGame(game.gameCode, {
            onSnapshot: (next) => {
                if (!active) return;
                setSnapshot((current) => latestSnapshot(current, next));
            },
            onClosed: () => {
                if (!active) return;
                clearHostGame();
                client.deactivate();
                setRoomClosed(true);
                setError("This room has been closed.");
            },
            onConnectionChange: (isConnected) => {
                if (!active) return;
                setConnected(isConnected);
                setConnectionLost(!isConnected && wasConnected);
                if (isConnected) {
                    wasConnected = true;
                    connectHost(client, game.gameCode, game.hostToken);
                }
            },
        });
        clientRef.current = client;

        return () => {
            active = false;
            client.deactivate();
            clientRef.current = null;
            setConnected(false);
            setConnectionLost(false);
        };
    }, [game]);

    const send = useCallback(
        (action: HostAction) => {
            if (!game || !clientRef.current) return;
            sendHostAction(
                clientRef.current,
                game.gameCode,
                game.hostToken,
                action,
            );
        },
        [game],
    );

    const startNextQuestion = useCallback(
        () => send("start-next-question"),
        [send],
    );
    const closeQuestion = useCallback(() => send("close-question"), [send]);
    const showLeaderboard = useCallback(() => send("show-leaderboard"), [send]);
    const finishGame = useCallback(() => send("finish"), [send]);

    const closeRoom = useCallback(() => {
        clearHostGame();
        setGame(null);
        setSnapshot(null);
        setError(null);
        setRoomClosed(false);
    }, []);

    return (
        <HostContext.Provider
            value={{
                game,
                snapshot,
                connected,
                connectionLost,
                error,
                roomClosed,
                openRoom,
                resumeRoom,
                startNextQuestion,
                closeQuestion,
                showLeaderboard,
                finishGame,
                closeRoom,
            }}
        >
            {children}
        </HostContext.Provider>
    );
}

export function useHost() {
    const context = useContext(HostContext);
    if (!context) {
        throw new Error("useHost must be used inside a HostProvider");
    }
    return context;
}
