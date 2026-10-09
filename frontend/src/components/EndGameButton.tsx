import { useState } from "react";
import ConfirmDialog from "./ConfirmDialog";
import { useHost } from "../context/HostContext";

export default function EndGameButton() {
    const { connected, finishGame } = useHost();
    const [confirming, setConfirming] = useState(false);

    return (
        <>
            <button
                type="button"
                className="button button-secondary"
                onClick={() => setConfirming(true)}
                disabled={!connected}
            >
                End game
            </button>

            <ConfirmDialog
                open={confirming}
                title="End the game?"
                message="The game stops right now and every player sees the final ranking."
                confirmLabel="End game"
                onConfirm={() => {
                    setConfirming(false);
                    finishGame();
                }}
                onCancel={() => setConfirming(false)}
            />
        </>
    );
}
