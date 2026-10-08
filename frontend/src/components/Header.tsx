import type { ReactNode } from "react";
import { Link } from "react-router";
import "../styles/Header.css";

export default function Header({ children }: { children?: ReactNode }) {
    return (
        <header className="header">
            <Link to="/" className="header-link">
                <img className="header-logo" src="/favicon.ico" alt="" />
                <span className="header-brand">Buzzer</span>
            </Link>
            {children}
        </header>
    );
}
