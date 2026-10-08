import { Link } from "react-router";
import Header from "../components/Header";
import "../styles/HomePage.css";

const steps = [
    {
        title: "Create a game",
        text: "Build your quiz question by question, then start the game with one click.",
    },
    {
        title: "Invite players",
        text: "Each player joins from their phone using the game code.",
    },
    {
        title: "Buzz in!",
        text: "Questions show up live: the fastest to answer scores the most points.",
    },
];

export default function HomePage() {
    return (
        <main className="home">
            <Header />

            <section className="home-actions">
                <Link to="/CreateQuizz" className="button home-button">
                    Create
                </Link>
                <Link to="/JoinQuizz" className="button home-button">
                    Join
                </Link>
            </section>

            <section className="home-presentation">
                <h1>The quiz you play live</h1>
                <p className="home-intro">
                    Buzzer is a real-time multiplayer quiz game. A host creates the game,
                    players join and answer questions as fast as they can.
                </p>

                <ol className="home-steps">
                    {steps.map((step, i) => (
                        <li key={step.title} className="home-step">
                            <span className="home-step-number">{i + 1}</span>
                            <div>
                                <h2>{step.title}</h2>
                                <p>{step.text}</p>
                            </div>
                        </li>
                    ))}
                </ol>
            </section>
        </main>
    );
}
