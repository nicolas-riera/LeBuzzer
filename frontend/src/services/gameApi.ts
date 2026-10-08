import type { GameSnapshot, HostGame } from "../types/game";

const HOST_GAME_KEY = "hostGame";

export async function createGame(): Promise<HostGame> {
    const response = await fetch("/api/games", { method: "POST" });
    if (!response.ok) {
        throw new Error(`Game creation failed (${response.status})`);
    }
    const game = await response.json();
    return { gameCode: game.gameCode, hostToken: game.hostToken };
}

export async function getSnapshot(
    gameCode: string,
): Promise<GameSnapshot | null> {
    const response = await fetch(`/api/games/${gameCode}/snapshot`);
    if (!response.ok) return null;
    return { ...(await response.json()), receivedAt: Date.now() };
}

export function saveHostGame(game: HostGame) {
    sessionStorage.setItem(HOST_GAME_KEY, JSON.stringify(game));
}

export function loadHostGame(): HostGame | null {
    const saved = sessionStorage.getItem(HOST_GAME_KEY);
    return saved ? JSON.parse(saved) : null;
}

export function clearHostGame() {
    sessionStorage.removeItem(HOST_GAME_KEY);
}
