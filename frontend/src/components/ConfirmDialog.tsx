import { useEffect, useRef } from "react";
import type { MouseEvent } from "react";
import "../styles/ConfirmDialog.css";

interface ConfirmDialogProps {
    open: boolean;
    title: string;
    message: string;
    confirmLabel: string;
    cancelLabel?: string;
    onConfirm: () => void;
    onCancel: () => void;
}

export default function ConfirmDialog({
    open,
    title,
    message,
    confirmLabel,
    cancelLabel = "Cancel",
    onConfirm,
    onCancel,
}: ConfirmDialogProps) {
    const dialogRef = useRef<HTMLDialogElement>(null);

    useEffect(() => {
        const dialog = dialogRef.current;
        if (!dialog) return;
        if (open && !dialog.open) dialog.showModal();
        if (!open && dialog.open) dialog.close();
    }, [open]);

    function handleBackdropClick(event: MouseEvent<HTMLDialogElement>) {
        if (event.target === event.currentTarget) onCancel();
    }

    return (
        <dialog
            ref={dialogRef}
            className="confirm-dialog"
            aria-labelledby="confirm-dialog-title"
            aria-describedby="confirm-dialog-message"
            onCancel={(event) => {
                event.preventDefault();
                onCancel();
            }}
            onClick={handleBackdropClick}
        >
            <div className="confirm-dialog-body">
                <h2 id="confirm-dialog-title" className="confirm-dialog-title">
                    {title}
                </h2>
                <p
                    id="confirm-dialog-message"
                    className="confirm-dialog-message"
                >
                    {message}
                </p>
                <div className="confirm-dialog-actions">
                    <button
                        type="button"
                        className="confirm-dialog-button"
                        onClick={onCancel}
                        autoFocus
                    >
                        {cancelLabel}
                    </button>
                    <button
                        type="button"
                        className="confirm-dialog-button confirm-dialog-button-danger"
                        onClick={onConfirm}
                    >
                        {confirmLabel}
                    </button>
                </div>
            </div>
        </dialog>
    );
}
