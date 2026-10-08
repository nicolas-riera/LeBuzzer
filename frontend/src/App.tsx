import { BrowserRouter, Route, Routes } from "react-router";
import { GameProvider } from "./context/GameContext";
import HomePage from "./pages/HomePage";
import CreateQuizz from "./pages/Host/CreateQuizz";
import JoinQuizz from "./pages/User/JoinQuizz";
import WaitingQuizz from "./pages/User/WaitingQuizz";

export default function App() {
    return (
        <GameProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/CreateQuizz" element={<CreateQuizz />} />
                    <Route
                        path="/JoinQuizz/:gameCode?"
                        element={<JoinQuizz />}
                    />
                    <Route
                        path="/WaitingQuizz/:gameCode"
                        element={<WaitingQuizz />}
                    />
                </Routes>
            </BrowserRouter>
        </GameProvider>
    );
}
