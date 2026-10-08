import { useEffect, useRef, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import Header from "../../components/Header";
import { useHost } from "../../context/HostContext";
import { useHostNavigation } from "../../hooks/useGameNavigation";
import "../../styles/CreateQuizz.css";

export default function CreateQuizz() {
    const {
        game,
        snapshot,
        connected,
        error,
        roomClosed,
        openRoom,
        startNextQuestion,
    } = useHost();
    const [ready, setReady] = useState(false);
    const [notice, setNotice] = useState<string | null>(null);
    const roomRequested = useRef(false);

    useHostNavigation(ready ? snapshot?.state : undefined);

    useEffect(() => {
        if (!roomClosed) return;
        setNotice(
            "Your previous room was closed because you were away for too long. Here is a new one.",
        );
        openRoom();
    }, [roomClosed, openRoom]);

    useEffect(() => {
        if (roomRequested.current) return;
        roomRequested.current = true;
        openRoom().then(() => setReady(true));
    }, [openRoom]);

    const players = snapshot?.onlinePlayers ?? [];
    const started = snapshot !== null && snapshot.state !== "WAITING";
    const joinUrl = game
        ? `${window.location.origin}/JoinQuizz/${game.gameCode}`
        : "";

    return (
        <main className="room">
            <Header />

            <div className="room-content">
                <h1 className="page-title room-title">Create a Room</h1>

                {notice && !error && (
                    <p className="room-notice" role="status">
                        {notice}
                    </p>
                )}

                {error && !roomClosed && (
                    <p className="room-error" role="alert">
                        {error}
                    </p>
                )}

                {!error && (!game || !ready) && (
                    <p className="room-loading">Creating the room…</p>
                )}

                {!error && game && ready && (
                    <>
                        <div className="room-code">
                            <span className="room-code-label">Room code</span>
                            <span className="room-code-value">
                                {game.gameCode}
                            </span>
                        </div>

                        <div className="room-qr">
                            <QRCodeSVG
                                value={joinUrl}
                                size={256}
                                bgColor="transparent"
                                fgColor="currentColor"
                                title={`Join room ${game.gameCode}`}
                            />
                        </div>

                        <section
                            className="room-players"
                            aria-labelledby="room-players-title"
                        >
                            <h2
                                id="room-players-title"
                                className="visually-hidden"
                            >
                                Players
                            </h2>
                            {players.length === 0 ? (
                                <p className="room-players-empty">
                                    Waiting for players…
                                </p>
                            ) : (
                                <ul className="room-players-list">
                                    {players.map((player) => (
                                        <li
                                            key={player}
                                            className="room-player"
                                        >
                                            {player}
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </section>

                        <p className="room-count" aria-live="polite">
                            {players.length}{" "}
                            {players.length === 1 ? "player" : "players"} online
                        </p>

                        <button
                            type="button"
                            className="button room-start"
                            onClick={startNextQuestion}
                            disabled={
                                !connected || players.length === 0 || started
                            }
                        >
                            {started ? "Started" : "Start"}
                        </button>
                    </>
                )}
            </div>
        </main>
    );
}
