import { BrowserRouter, Route, Routes } from "react-router";
import RequireHost from "./components/RequireHost";
import RequirePlayer from "./components/RequirePlayer";
import { GameProvider } from "./context/GameContext";
import { HostProvider } from "./context/HostContext";
import HomePage from "./pages/HomePage";
import AnswerHost from "./pages/Host/AnswerHost";
import CreateQuiz from "./pages/Host/CreateQuiz";
import GameOverHost from "./pages/Host/GameOverHost";
import LeaderboardHost from "./pages/Host/LeaderboardHost";
import QuestionHost from "./pages/Host/QuestionHost";
import AnswerUser from "./pages/User/AnswerUser";
import GameOverUser from "./pages/User/GameOverUser";
import JoinQuiz from "./pages/User/JoinQuiz";
import QuestionUser from "./pages/User/QuestionUser";
import WaitingQuiz from "./pages/User/WaitingQuiz";

export default function App() {
    return (
        <HostProvider>
            <GameProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/CreateQuiz" element={<CreateQuiz />} />
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
                            path="/JoinQuiz/:gameCode?"
                            element={<JoinQuiz />}
                        />
                        <Route
                            path="/WaitingQuiz/:gameCode"
                            element={
                                <RequirePlayer>
                                    <WaitingQuiz />
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
