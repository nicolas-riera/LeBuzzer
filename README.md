# LeBuzzer

A real-time quiz game where the fastest buzzer wins.

## Presentation

LeBuzzer is a full-stack school project consisting of a multiplayer quiz web application, in the spirit of Kahoot. A host creates a room and displays the questions on a shared screen, while players join from their phone and answer as fast as they can. It pairs a Spring Boot backend communicating over WebSockets with a modern web frontend.

### Key Features
- Rooms: Join with a 5-letter code or by scanning a QR code.
- Real-time Gameplay: Questions, answers and scores synced live through WebSockets.
- Quiz: 10 random questions out of 50, across 5 themes.
- Speed-based Scoring: The faster the right answer, the more points.
- Leaderboard: Ranking between questions and at the end of the game.
- Host Controls: Start or close questions, show the leaderboard and end the game.
- Reconnection: Players and host can reload the page without leaving the game.
- Haptic Feedback: Phones vibrate when buzzing in.

### Tech Stack
- Backend: Java 25, Spring Boot 4, Spring WebSocket (STOMP).
- Frontend: React 19, TypeScript, Vite, React Router, STOMP.js & SockJS.
- Documentation & Tooling: Swagger UI (Springdoc), GitHub Actions.

### Repository Structure

```
LeBuzzer/
├── .github/workflows   # Github actions
├── backend/            # Spring Boot application
├── frontend/           # React + TypeScript application
├── docs/               # Project specifications
└── README.md           # Setup instructions & project documentation
```

### Authors

This project has been realized by [Nicolas](https://github.com/nicolas-riera/) and [Gabriel](https://github.com/Gabriel-SEMPERE/).

## Setup and run

### Requirements

- JDK 25 (bin folder in ```PATH``` environment variable)
- Maven (also in ```PATH```)
- Node.JS

Start by cloning the repository:

```batch
git clone https://github.com/nicolas-riera/LeBuzzer.git
```

```batch
cd LeBuzzer
```

*No database is needed: games are stored in memory and are lost when the backend stops.*

### Backend Setup

#### Run Springboot

While being at the root of the repository, run:

```batch
mvn spring-boot:run -f backend
```

*The backend runs on port 8080.*

#### Run Tests

You can also run the tests made, to check if the code works:

```batch
mvn clean test -f backend/pom.xml
```

#### Access SwaggerUI

You can access SwaggerUI on [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/). This will tell all the REST endpoints and let you test them.

*Note: the game itself (joining, answering, host actions) goes through WebSockets on the ```/ws``` endpoint, which is not listed in SwaggerUI.*

### Frontend Setup

While being at the root of the repository, install the dependencies:

```batch
npm --prefix frontend install
```

then run:

```batch
npm --prefix frontend run dev
```

The website should be accessible on [http://localhost:5173/](http://localhost:5173/).

#### Play from your phone

To let players join from their phone on the same network, expose the frontend on your local network:

```batch
npm --prefix frontend run dev -- --host
```

Then open the website from the **Network** address shown by Vite (for example ```http://192.168.1.10:5173/```) before creating a room, so the QR code points to an address reachable by the phones.

*The backend must be running as well, the frontend forwards ```/api``` and ```/ws``` requests to it.*
