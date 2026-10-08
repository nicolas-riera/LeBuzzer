import type {
    GameSnapshot,
    HostGame,
    StoredPlayerSession,
} from "../types/game";

const HOST_GAME_KEY = "hostGame";
const PLAYER_SESSION_KEY = "playerSession";

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
    if (response.status === 404) return null;
    if (!response.ok) {
        throw new Error(`Snapshot request failed (${response.status})`);
    }
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

export function savePlayerSession(session: StoredPlayerSession) {
    sessionStorage.setItem(PLAYER_SESSION_KEY, JSON.stringify(session));
}

export function loadPlayerSession(): StoredPlayerSession | null {
    const saved = sessionStorage.getItem(PLAYER_SESSION_KEY);
    return saved ? JSON.parse(saved) : null;
}

export function clearPlayerSession() {
    sessionStorage.removeItem(PLAYER_SESSION_KEY);
}

export function createPlayerToken() {
    const bytes = crypto.getRandomValues(new Uint8Array(16));
    return Array.from(bytes, (byte) => byte.toString(16).padStart(2, "0")).join(
        "",
    );
}
