import { Client, ReconnectionTimeMode } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getSnapshot } from "./gameApi";
import type { GameSnapshot, JoinedPlayer } from "../types/game";

const FIRST_RECONNECT_DELAY_MILLIS = 1000;
const MAX_RECONNECT_DELAY_MILLIS = 30000;
const HEARTBEAT_MILLIS = 5000;

export type HostAction =
    "start-next-question" | "close-question" | "show-leaderboard" | "finish";

interface GameSocketHandlers {
    onSnapshot: (snapshot: GameSnapshot) => void;
    onPlayerJoined?: (player: JoinedPlayer) => void;
    onClosed?: () => void;
    onConnectionChange?: (connected: boolean) => void;
}

export function connectToGame(
    gameCode: string,
    handlers: GameSocketHandlers,
): Client {
    const client = new Client({
        webSocketFactory: () => new SockJS("/ws"),
        reconnectDelay: FIRST_RECONNECT_DELAY_MILLIS,
        reconnectTimeMode: ReconnectionTimeMode.EXPONENTIAL,
        maxReconnectDelay: MAX_RECONNECT_DELAY_MILLIS,
        heartbeatIncoming: HEARTBEAT_MILLIS,
        heartbeatOutgoing: HEARTBEAT_MILLIS,
        onConnect: () => {
            client.subscribe(`/topic/game/${gameCode}`, (message) => {
                handlers.onSnapshot({
                    ...JSON.parse(message.body),
                    receivedAt: Date.now(),
                });
            });
            if (handlers.onPlayerJoined) {
                const onPlayerJoined = handlers.onPlayerJoined;
                client.subscribe(
                    `/topic/game/${gameCode}/player-joined`,
                    (message) => {
                        onPlayerJoined(JSON.parse(message.body));
                    },
                );
            }
            if (handlers.onClosed) {
                const onClosed = handlers.onClosed;
                client.subscribe(`/topic/game/${gameCode}/closed`, () =>
                    onClosed(),
                );
            }
            handlers.onConnectionChange?.(true);
            getSnapshot(gameCode)
                .then((current) => {
                    if (current) handlers.onSnapshot(current);
                    else handlers.onClosed?.();
                })
                .catch(() => undefined);
        },
        onWebSocketClose: () => handlers.onConnectionChange?.(false),
    });
    client.activate();
    return client;
}

export function connectHost(
    client: Client,
    gameCode: string,
    hostToken: string,
) {
    client.publish({
        destination: `/app/game/${gameCode}/host`,
        headers: { hostToken },
    });
}

export function joinGame(
    client: Client,
    gameCode: string,
    nickname: string,
    token: string,
) {
    client.publish({
        destination: `/app/game/${gameCode}/join`,
        body: JSON.stringify({ nickname, token }),
    });
}

export function submitAnswer(
    client: Client,
    gameCode: string,
    selectedIndices: number[],
) {
    client.publish({
        destination: `/app/game/${gameCode}/submit-answer`,
        body: JSON.stringify({ selectedIndices }),
    });
}

export function sendHostAction(
    client: Client,
    gameCode: string,
    hostToken: string,
    action: HostAction,
) {
    client.publish({
        destination: `/app/game/${gameCode}/${action}`,
        headers: { hostToken },
    });
}
