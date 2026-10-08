import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router";
import type { GameState } from "../types/game";

const HOST_ROUTES: Partial<Record<GameState, string>> = {
    WAITING: "/CreateQuizz",
    QUIZZING: "/QuestionHost",
    QUIZ_RESULTS: "/AnswerHost",
    LEADERBOARD: "/AnswerHost",
};

const PLAYER_ROUTES: Partial<Record<GameState, string>> = {
    WAITING: "/WaitingQuizz",
    QUIZZING: "/QuestionUser",
    QUIZ_RESULTS: "/AnswerUser",
    LEADERBOARD: "/AnswerUser",
};

function useFollowRoute(target: string | undefined) {
    const navigate = useNavigate();
    const { pathname } = useLocation();

    useEffect(() => {
        if (target && target.toLowerCase() !== pathname.toLowerCase()) {
            navigate(target, { replace: true });
        }
    }, [target, pathname, navigate]);
}

export function useHostNavigation(state: GameState | undefined) {
    useFollowRoute(state && HOST_ROUTES[state]);
}

export function usePlayerNavigation(
    gameCode: string,
    state: GameState | undefined,
) {
    const route = state && PLAYER_ROUTES[state];
    useFollowRoute(route && `${route}/${gameCode}`);
}
