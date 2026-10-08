import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { GameSnapshot, Player } from "../types/game";

interface GameSocketHandlers {
    onSnapshot: (snapshot: GameSnapshot) => void;
    onPlayerJoined?: (player: Player) => void;
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
                handlers.onSnapshot(JSON.parse(message.body));
            });
            if (handlers.onPlayerJoined) {
                const onPlayerJoined = handlers.onPlayerJoined;
                client.subscribe(`/topic/game/${gameCode}/player-joined`, (message) => {
                    onPlayerJoined(JSON.parse(message.body));
                });
            }
            handlers.onConnectionChange?.(true);
        },
        onWebSocketClose: () => handlers.onConnectionChange?.(false),
    });
    client.activate();
    return client;
}

export function joinGame(client: Client, gameCode: string, nickname: string) {
    client.publish({
        destination: `/app/game/${gameCode}/join`,
        body: JSON.stringify({ nickname }),
    });
}

export function startNextQuestion(
    client: Client,
    gameCode: string,
    hostToken: string,
) {
    client.publish({
        destination: `/app/game/${gameCode}/start-next-question`,
        headers: { hostToken },
    });
}
