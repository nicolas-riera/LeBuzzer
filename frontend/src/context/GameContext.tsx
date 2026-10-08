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
import { getSnapshot } from "../services/gameApi";
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

const JOIN_TIMEOUT_MS = 5000;

interface GameContextValue {
    session: PlayerSession | null;
    snapshot: GameSnapshot | null;
    closedGameCode: string | null;
    answer: SubmittedAnswer | null;
    join: (gameCode: string, nickname: string) => Promise<void>;
    leave: () => void;
    submitAnswer: (selectedIndices: number[]) => void;
}

const GameContext = createContext<GameContextValue | null>(null);

export function GameProvider({ children }: { children: ReactNode }) {
    const [session, setSession] = useState<PlayerSession | null>(null);
    const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null);
    const [closedGameCode, setClosedGameCode] = useState<string | null>(null);
    const [answer, setAnswer] = useState<SubmittedAnswer | null>(null);
    const clientRef = useRef<Client | null>(null);
    const finishedRef = useRef(false);

    const leave = useCallback(() => {
        clientRef.current?.deactivate();
        clientRef.current = null;
        setSession(null);
        setSnapshot(null);
        setAnswer(null);
    }, []);

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

            const current = await getSnapshot(code);
            if (!current) {
                throw new Error("This room does not exist.");
            }
            if (current.state === "FINISHED") {
                throw new Error("This game is already over.");
            }
            if (!current.hostConnected) {
                throw new Error(
                    "This room has no host. Ask the host to open it again.",
                );
            }
            if (
                current.onlinePlayers.some(
                    (player) => player.toLowerCase() === name.toLowerCase(),
                )
            ) {
                throw new Error("This nickname is already taken.");
            }

            leave();
            finishedRef.current = false;
            setClosedGameCode(null);
            setSnapshot(current);

            await new Promise<void>((resolve, reject) => {
                let joined = false;

                const fail = (message: string) => {
                    clearTimeout(timeout);
                    client.deactivate();
                    clientRef.current = null;
                    reject(new Error(message));
                };

                const timeout = setTimeout(
                    () => fail("Unable to join the room. Please try again."),
                    JOIN_TIMEOUT_MS,
                );

                const client = connectToGame(code, {
                    onSnapshot: (next) => {
                        finishedRef.current = next.state === "FINISHED";
                        setSnapshot(next);
                    },
                    onPlayerJoined: (player) => {
                        if (
                            joined ||
                            player.id.toLowerCase() !== name.toLowerCase()
                        )
                            return;
                        joined = true;
                        clearTimeout(timeout);
                        setSession({ gameCode: code, nickname: player.id });
                        resolve();
                    },
                    onClosed: () => {
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
                    onConnectionChange: (connected) => {
                        if (connected) joinGame(client, code, name);
                    },
                });
                clientRef.current = client;
            });
        },
        [leave],
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
                closedGameCode,
                answer,
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
