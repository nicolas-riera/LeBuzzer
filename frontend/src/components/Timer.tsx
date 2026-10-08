import { useEffect, useState } from "react";
import "../styles/Timer.css";

const URGENT_SECONDS = 5;

function formatTime(totalSeconds: number) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

export default function Timer({
    remainingMillis,
}: {
    remainingMillis: number;
}) {
    const [deadline, setDeadline] = useState(
        () => Date.now() + remainingMillis,
    );
    const [now, setNow] = useState(() => Date.now());

    useEffect(() => {
        setDeadline(Date.now() + remainingMillis);
    }, [remainingMillis]);

    useEffect(() => {
        const interval = setInterval(() => setNow(Date.now()), 250);
        return () => clearInterval(interval);
    }, []);

    const seconds = Math.max(0, Math.ceil((deadline - now) / 1000));
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
