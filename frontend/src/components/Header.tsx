import type { ReactNode } from "react";
import { Link } from "react-router";
import "../styles/Header.css";

interface HeaderProps {
    gameCode?: string;
    children?: ReactNode;
}

export default function Header({ gameCode, children }: HeaderProps) {
    return (
        <header className="header">
            <Link to="/" className="header-link">
                <img className="header-logo" src="/favicon.ico" alt="" />
                <span className="header-brand">Buzzer</span>
            </Link>
            {gameCode && (
                <p className="header-room">
                    <span className="label">Room</span>
                    <span className="header-room-code">{gameCode}</span>
                </p>
            )}
            {children && <div className="header-end">{children}</div>}
        </header>
    );
}
