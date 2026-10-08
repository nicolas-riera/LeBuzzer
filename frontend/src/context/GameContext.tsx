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
    clearPlayerSession,
    createPlayerToken,
    getSnapshot,
    loadPlayerSession,
    savePlayerSession,
} from "../services/gameApi";
import {
    connectToGame,
    joinGame,
    submitAnswer as sendAnswer,
} from "../services/gameSocket";
import type {
    GameSnapshot,
    PlayerSession,
    SubmittedAnswer,
} from "../types/game";
import { latestSnapshot } from "../utils/snapshot";

const JOIN_TIMEOUT_MS = 8000;

interface GameContextValue {
    session: PlayerSession | null;
    snapshot: GameSnapshot | null;
    connected: boolean;
    closedGameCode: string | null;
    answer: SubmittedAnswer | null;
    joinedAtQuestion: number | null;
    join: (gameCode: string, nickname: string) => Promise<void>;
    leave: () => void;
    submitAnswer: (selectedIndices: number[]) => void;
}

const GameContext = createContext<GameContextValue | null>(null);

export function GameProvider({ children }: { children: ReactNode }) {
    const [session, setSession] = useState<PlayerSession | null>(null);
    const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null);
    const [connected, setConnected] = useState(false);
    const [closedGameCode, setClosedGameCode] = useState<string | null>(null);
    const [answer, setAnswer] = useState<SubmittedAnswer | null>(null);
    const [joinedAtQuestion, setJoinedAtQuestion] = useState<number | null>(
        null,
    );
    const clientRef = useRef<Client | null>(null);
    const finishedRef = useRef(false);

    const applySnapshot = useCallback((next: GameSnapshot) => {
        if (next.state === "FINISHED") finishedRef.current = true;
        setSnapshot((current) => latestSnapshot(current, next));
    }, []);

    const disconnect = useCallback(() => {
        clientRef.current?.deactivate();
        clientRef.current = null;
        setSession(null);
        setSnapshot(null);
        setConnected(false);
        setAnswer(null);
        setJoinedAtQuestion(null);
    }, []);

    const leave = useCallback(() => {
        disconnect();
        clearPlayerSession();
    }, [disconnect]);

    const submitAnswer = useCallback(
        (selectedIndices: number[]) => {
            const question = snapshot?.currentQuestion;
            if (!session || !question || !clientRef.current) return;
            const scoreBefore =
                snapshot.leaderboard.find(
                    (entry) => entry.playerId === session.nickname,
                )?.score ?? 0;
            sendAnswer(clientRef.current, session.gameCode, selectedIndices);
            setAnswer({
                questionNumber: question.number,
                selectedIndices,
                scoreBefore,
            });
        },
        [session, snapshot],
    );

    const join = useCallback(
        async (gameCode: string, nickname: string) => {
            const code = gameCode.trim().toUpperCase();
            const name = nickname.trim();
            const stored = loadPlayerSession();
            const resuming =
                stored !== null &&
                stored.gameCode === code &&
                stored.nickname.toLowerCase() === name.toLowerCase();
            const token = resuming ? stored.token : createPlayerToken();

            const current = await getSnapshot(code);
            if (!current) {
                if (resuming) clearPlayerSession();
                throw new Error("This room does not exist.");
            }
            if (current.state === "FINISHED") {
                throw new Error("This game is already over.");
            }
            if (!resuming && !current.hostConnected) {
                throw new Error(
                    "This room has no host. Ask the host to open it again.",
                );
            }
            if (
                !resuming &&
                current.onlinePlayers.some(
                    (player) => player.toLowerCase() === name.toLowerCase(),
                )
            ) {
                throw new Error("This nickname is already taken.");
            }

            disconnect();
            finishedRef.current = false;
            setClosedGameCode(null);
            setJoinedAtQuestion(
                resuming || current.state === "WAITING"
                    ? null
                    : (current.currentQuestion?.number ?? null),
            );
            applySnapshot(current);

            await new Promise<void>((resolve, reject) => {
                let joined = false;

                const fail = (message: string) => {
                    clearTimeout(timeout);
                    client.deactivate();
                    clientRef.current = null;
                    setConnected(false);
                    reject(new Error(message));
                };

                const timeout = setTimeout(
                    () => fail("Unable to join the room. Please try again."),
                    JOIN_TIMEOUT_MS,
                );

                const isCurrent = () => clientRef.current === client;

                const client = connectToGame(code, {
                    onSnapshot: (next) => {
                        if (isCurrent()) applySnapshot(next);
                    },
                    onPlayerJoined: (player) => {
                        if (
                            joined ||
                            player.id.toLowerCase() !== name.toLowerCase()
                        ) {
                            return;
                        }
                        joined = true;
                        clearTimeout(timeout);
                        setSession({ gameCode: code, nickname: player.id });
                        savePlayerSession({
                            gameCode: code,
                            nickname: player.id,
                            token,
                        });
                        resolve();
                    },
                    onClosed: () => {
                        clearPlayerSession();
                        if (!joined) {
                            fail("This room has been closed.");
                            return;
                        }
                        client.deactivate();
                        clientRef.current = null;
                        if (finishedRef.current) return;
                        setSession(null);
                        setSnapshot(null);
                        setClosedGameCode(code);
                    },
                    onConnectionChange: (isConnected) => {
                        if (!isCurrent()) return;
                        setConnected(isConnected);
                        if (isConnected) joinGame(client, code, name, token);
                    },
                });
                clientRef.current = client;
            });
        },
        [applySnapshot, disconnect],
    );

    useEffect(
        () => () => {
            clientRef.current?.deactivate();
        },
        [],
    );

    return (
        <GameContext.Provider
            value={{
                session,
                snapshot,
                connected,
                closedGameCode,
                answer,
                joinedAtQuestion,
                join,
                leave,
                submitAnswer,
            }}
        >
            {children}
        </GameContext.Provider>
    );
}

export function useGame() {
    const context = useContext(GameContext);
    if (!context) {
        throw new Error("useGame must be used inside a GameProvider");
    }
    return context;
}
