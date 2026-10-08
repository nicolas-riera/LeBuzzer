import { useEffect, useState } from "react";
import type { GameSnapshot } from "../types/game";
import "../styles/Timer.css";

const URGENT_SECONDS = 5;
const TICK_MILLIS = 200;

interface Deadline {
    questionNumber: number;
    endsAt: number;
}

function formatTime(totalSeconds: number) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

export default function Timer({ snapshot }: { snapshot: GameSnapshot }) {
    const questionNumber = snapshot.currentQuestion?.number ?? 0;
    const durationMillis =
        (snapshot.currentQuestion?.durationInSeconds ?? 0) * 1000;
    const estimate =
        snapshot.receivedAt +
        Math.min(snapshot.remainingMillis, durationMillis);

    const [deadline, setDeadline] = useState<Deadline>({
        questionNumber,
        endsAt: estimate,
    });
    const [now, setNow] = useState(() => Date.now());

    if (
        deadline.questionNumber !== questionNumber ||
        estimate < deadline.endsAt
    ) {
        setDeadline({ questionNumber, endsAt: estimate });
    }

    useEffect(() => {
        const interval = setInterval(() => setNow(Date.now()), TICK_MILLIS);
        return () => clearInterval(interval);
    }, []);

    const seconds = Math.max(
        0,
        Math.min(
            Math.ceil((deadline.endsAt - now) / 1000),
            durationMillis / 1000,
        ),
    );
    const urgent = seconds <= URGENT_SECONDS;

    return (
        <time
            className={urgent ? "timer timer-urgent" : "timer"}
            aria-label={`${seconds} seconds left`}
        >
            {formatTime(seconds)}
        </time>
    );
}
