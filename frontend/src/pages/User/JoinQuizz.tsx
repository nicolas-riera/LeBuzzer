import { useState } from "react";
import type { SubmitEvent } from "react";
import { useLocation, useNavigate, useParams } from "react-router";
import Header from "../../components/Header";
import { useGame } from "../../context/GameContext";
import "../../styles/JoinQuizz.css";

const CODE_LENGTH = 5;
const NICKNAME_MAX_LENGTH = 20;

export default function JoinQuizz() {
    const { gameCode: codeFromUrl } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { join } = useGame();

    const [code, setCode] = useState(
        (codeFromUrl ?? "").toUpperCase().slice(0, CODE_LENGTH),
    );
    const [nickname, setNickname] = useState("");
    const [joining, setJoining] = useState(false);
    const [error, setError] = useState<string | null>(
        location.state?.error ?? null,
    );

    const canSubmit =
        code.length === CODE_LENGTH && nickname.trim().length > 0 && !joining;

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setJoining(true);
        setError(null);
        try {
            await join(code, nickname);
            navigate(`/WaitingQuizz/${code}`);
        } catch (joinError) {
            setError(
                joinError instanceof Error
                    ? joinError.message
                    : "Unable to join the room.",
            );
            setJoining(false);
        }
    }

    return (
        <main className="join">
            <Header />

            <div className="join-content">
                <h1 className="page-title">Join a Room</h1>

                <form className="join-form" onSubmit={handleSubmit} noValidate>
                    <label className="visually-hidden" htmlFor="join-code">
                        Room code
                    </label>
                    <input
                        id="join-code"
                        className="join-input join-input-code"
                        placeholder="Code"
                        value={code}
                        onChange={(event) =>
                            setCode(
                                event.target.value
                                    .toUpperCase()
                                    .replace(/[^A-Z]/g, "")
                                    .slice(0, CODE_LENGTH),
                            )
                        }
                        autoCapitalize="characters"
                        autoComplete="off"
                        spellCheck={false}
                        autoFocus={!codeFromUrl}
                        disabled={joining}
                    />

                    <label className="visually-hidden" htmlFor="join-nickname">
                        Nickname
                    </label>
                    <input
                        id="join-nickname"
                        className="join-input"
                        placeholder="Name"
                        value={nickname}
                        onChange={(event) => setNickname(event.target.value)}
                        maxLength={NICKNAME_MAX_LENGTH}
                        autoComplete="nickname"
                        autoFocus={Boolean(codeFromUrl)}
                        disabled={joining}
                    />

                    {error && (
                        <p className="join-error" role="alert">
                            {error}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="button join-submit"
                        disabled={!canSubmit}
                    >
                        {joining ? "Joining…" : "Confirm"}
                    </button>
                </form>
            </div>
        </main>
    );
}
