export type GameState =
    "WAITING" | "QUIZZING" | "QUIZ_RESULTS" | "LEADERBOARD" | "FINISHED";

export interface QuestionView {
    id: number;
    number: number;
    totalQuestions: number;
    text: string;
    options: string[];
    multipleChoice: boolean;
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
    receivedAt: number;
    hostConnected: boolean;
    sequence: number;
}

export interface JoinedPlayer {
    id: string;
    score: number;
}

export interface PlayerSession {
    gameCode: string;
    nickname: string;
}

export interface StoredPlayerSession extends PlayerSession {
    token: string;
}

export interface SubmittedAnswer {
    questionNumber: number;
    selectedIndices: number[];
    scoreBefore: number;
}

export interface HostGame {
    gameCode: string;
    hostToken: string;
}
