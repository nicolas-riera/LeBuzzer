import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { GameSnapshot, Player } from "../types/game";

export type HostAction =
    "start-next-question" | "close-question" | "show-leaderboard" | "finish";

interface GameSocketHandlers {
    onSnapshot: (snapshot: GameSnapshot) => void;
    onPlayerJoined?: (player: Player) => void;
    onClosed?: () => void;
    onConnectionChange?: (connected: boolean) => void;
}

export function connectToGame(
    gameCode: string,
    handlers: GameSocketHandlers,
): Client {
    const client = new Client({
        webSocketFactory: () => new SockJS("/ws"),
        reconnectDelay: 3000,
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

export function joinGame(client: Client, gameCode: string, nickname: string) {
    client.publish({
        destination: `/app/game/${gameCode}/join`,
        body: JSON.stringify({ nickname }),
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
