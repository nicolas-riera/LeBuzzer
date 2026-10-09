import "../styles/ConnectionStatus.css";

export default function ConnectionStatus({
    connected,
}: {
    connected: boolean;
}) {
    return (
        <div
            className={
                connected
                    ? "connection-status"
                    : "connection-status connection-status-offline"
            }
            role="status"
        >
            <span className="connection-status-dot" aria-hidden="true" />
            {connected ? "Live" : "Offline"}
        </div>
    );
}
