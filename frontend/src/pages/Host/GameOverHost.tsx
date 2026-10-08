import { Link } from "react-router";
import Header from "../../components/Header";
import Leaderboard from "../../components/Leaderboard";
import { useHost } from "../../context/HostContext";
import "../../styles/GameOver.css";

export default function GameOverHost() {
    const { snapshot } = useHost();

    if (!snapshot) return null;

    return (
        <main className="gameover">
            <Header />

            <div className="gameover-content">
                <h1 className="page-title gameover-title">Best players</h1>

                <Leaderboard entries={snapshot.leaderboard} />

                <div className="gameover-actions">
                    <Link to="/" className="button">
                        Game over
                    </Link>
                </div>
            </div>
        </main>
    );
}
