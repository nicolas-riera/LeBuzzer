export type GameState =
    "WAITING" | "QUIZZING" | "QUIZ_RESULTS" | "LEADERBOARD" | "FINISHED";

export interface QuestionView {
    id: number;
    text: string;
    options: string[];
    durationInSeconds: number;
}

export interface LeaderboardEntry {
    rank: number;
    playerId: string;
    score: number;
}

export interface GameSnapshot {
    gameCode: string;
    state: GameState;
    onlinePlayers: string[];
    currentQuestion: QuestionView | null;
    remainingMillis: number;
    answeredCount: number;
    correctAnswerIndices: number[] | null;
    leaderboard: LeaderboardEntry[];
    hostConnected: boolean;
}

export interface Player {
    id: string;
    score: number;
    playerStatus: "ONLINE" | "OFFLINE";
}

export interface PlayerSession {
    gameCode: string;
    nickname: string;
}

export interface HostGame {
    gameCode: string;
    hostToken: string;
}
