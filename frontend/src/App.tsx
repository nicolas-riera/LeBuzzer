import { BrowserRouter, Route, Routes } from "react-router";
import RequireHost from "./components/RequireHost";
import RequirePlayer from "./components/RequirePlayer";
import { GameProvider } from "./context/GameContext";
import { HostProvider } from "./context/HostContext";
import HomePage from "./pages/HomePage";
import AnswerHost from "./pages/Host/AnswerHost";
import CreateQuizz from "./pages/Host/CreateQuizz";
import GameOverHost from "./pages/Host/GameOverHost";
import LeaderboardHost from "./pages/Host/LeaderboardHost";
import QuestionHost from "./pages/Host/QuestionHost";
import AnswerUser from "./pages/User/AnswerUser";
import GameOverUser from "./pages/User/GameOverUser";
import JoinQuizz from "./pages/User/JoinQuizz";
import QuestionUser from "./pages/User/QuestionUser";
import WaitingQuizz from "./pages/User/WaitingQuizz";

export default function App() {
    return (
        <HostProvider>
            <GameProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/CreateQuizz" element={<CreateQuizz />} />
                        <Route
                            path="/QuestionHost"
                            element={
                                <RequireHost>
                                    <QuestionHost />
                                </RequireHost>
                            }
                        />
                        <Route
                            path="/AnswerHost"
                            element={
                                <RequireHost>
                                    <AnswerHost />
                                </RequireHost>
                            }
                        />
                        <Route
                            path="/LeaderboardHost"
                            element={
                                <RequireHost>
                                    <LeaderboardHost />
                                </RequireHost>
                            }
                        />
                        <Route
                            path="/GameOverHost"
                            element={
                                <RequireHost>
                                    <GameOverHost />
                                </RequireHost>
                            }
                        />
                        <Route
                            path="/JoinQuizz/:gameCode?"
                            element={<JoinQuizz />}
                        />
                        <Route
                            path="/WaitingQuizz/:gameCode"
                            element={
                                <RequirePlayer>
                                    <WaitingQuizz />
                                </RequirePlayer>
                            }
                        />
                        <Route
                            path="/QuestionUser/:gameCode"
                            element={
                                <RequirePlayer>
                                    <QuestionUser />
                                </RequirePlayer>
                            }
                        />
                        <Route
                            path="/AnswerUser/:gameCode"
                            element={
                                <RequirePlayer>
                                    <AnswerUser />
                                </RequirePlayer>
                            }
                        />
                        <Route
                            path="/GameOverUser/:gameCode"
                            element={
                                <RequirePlayer>
                                    <GameOverUser />
                                </RequirePlayer>
                            }
                        />
                    </Routes>
                </BrowserRouter>
            </GameProvider>
        </HostProvider>
    );
}
