import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "@fontsource/instrument-sans/latin-400.css";
import "@fontsource/instrument-sans/latin-600.css";
import "./styles/fonts.css";
import "./styles/global.css";
import App from "./App.tsx";

createRoot(document.getElementById("app")!).render(
    <StrictMode>
        <App />
    </StrictMode>,
);
