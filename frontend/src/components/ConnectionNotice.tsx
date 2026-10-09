import "../styles/ConnectionNotice.css";

export default function ConnectionNotice({ message }: { message: string }) {
    return (
        <div className="connection-notice" role="status">
            <span className="connection-notice-dot" aria-hidden="true" />
            {message}
        </div>
    );
}
