import type { GameSnapshot } from "../types/game";

export function latestSnapshot(
    current: GameSnapshot | null,
    next: GameSnapshot,
) {
    if (
        !current ||
        current.gameCode !== next.gameCode ||
        next.sequence > current.sequence
    ) {
        return next;
    }
    return current;
}
