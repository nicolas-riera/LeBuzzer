# LeBuzzer: HTTP Polling vs WebSockets

A comparative study, with figures, of the two ways to push the game state to the players.

## Reference Scenario

| Parameter          | Value                                       |
| ------------------ | ------------------------------------------- |
| Game length        | 10 minutes (600 s)                          |
| Connected players  | 15 (plus 1 host)                            |
| Polling interval   | 2 seconds                                   |
| Game content       | 10 questions, then leaderboard and end game |

### Methodology

The WebSocket numbers are measured, not estimated. A Node.js script played a full 10-minute game against the real Spring Boot backend, using the same libraries and settings as the frontend (`@stomp/stompjs` 7.3, `sockjs-client` 1.6, 5 s STOMP heartbeats):

- 1 host and 15 players joined the room over SockJS/STOMP during the first 20 seconds.
- The host started 10 questions, closed half of them manually (the other half ended on the timer, with 2 players not answering), showed the leaderboard after each one, and ended the game at 9:40.
- 140 answers were submitted in total.
- Every frame sent and received by each client was counted and sorted by type. Raw WebSocket frames and TCP bytes were counted too.
- During the same game, 15 extra clients polled `GET /api/games/{code}/snapshot` every 2 seconds, so both approaches were measured on the same events.

Everything ran on one machine (localhost), so the network round trip is close to 0. On a real Wi-Fi network you would add about 10 to 50 ms to both approaches

## 1. HTTP Polling: Request Count

### Theory

```
600 s / 2 s = 300 requests per player
300 x 15 players = 4,500 requests (and 4,500 responses)
```

If the host screen also polls, the total becomes 300 x 16 = 4,800 requests.

### Measured

| Metric                               | Value                  |
| ------------------------------------ | ---------------------- |
| HTTP requests sent                   | **4,500**              |
| Responses that contained a change    | 1,499 (33%)            |
| Responses identical to the previous* | 3,001 (**67% wasted**) |
| Traffic on the wire (TCP, both ways) | 5.97 MB                |
| Average size per exchange            | ~1.3 KB (~1.07 KB body + headers) |

Compared without the `sequence` number and the timer value, which change on every read.

The number of requests does not depend on what happens in the game: a quiet lobby costs as much as a question with 15 players answering.

## 2. WebSocket: Measured Messages

### Total

| Category                                              | Messages  | Share |
| ----------------------------------------------------- | --------- | ----- |
| Game messages (snapshots, actions, answers, joins)    | 3,314     | 53%   |
| Heartbeats (client to server: 1,879, server to client: 960) | 2,839 | 45% |
| Session setup and teardown (CONNECT, SUBSCRIBE, ...)  | 126       | 2%    |
| **Total STOMP messages**                              | **6,279** | 100%  |

*At the transport level this gives 6,295 WebSocket frames: the 16 extra frames are the SockJS "open" frames. Traffic on the wire was 4.19 MB.*

The WebSocket implementation also makes 49 HTTP requests: 1 room creation, 32 snapshot reads (one before joining and one after each connection, to resynchronise) and 16 SockJS `/ws/info` requests.

### Game Messages in Detail

| Direction        | Message                                           | Count |
| ---------------- | ------------------------------------------------- | ----- |
| Client to server | `submit-answer`                                   | 140   |
| Client to server | `join` (15), `host` (1)                           | 16    |
| Client to server | Host actions (start 10, close 5, leaderboard 10, finish 1) | 26 |
| Server to client | Game snapshot (`/topic/game/{code}`)              | 2,872 |
| Server to client | Personal answer (`/user/queue/answer`)            | 140   |
| Server to client | `player-joined`                                   | 120   |

### What the Numbers Show

-The server sent 187 snapshot broadcasts, and each of them reached 16 clients. 140 of them were caused by a player answering (the host sees the answer counter go up). Broadcast traffic therefore grows with *players x answers*, i.e. quadratically: with 50 players it would be about 50 x 10 x 51 = 25,500 snapshots.
-Almost half of the messages are heartbeats. They are only used to detect dead connections. The server skips its heartbeat when it has sent something recently (960 instead of ~1,900), the client does not.
- In bytes, WebSockets only save 30% (4.19 MB vs 5.97 MB). The full snapshot (~1 KB) is resent on every change, even when only `answeredCount` moved. Sending the answer counter to the host only, or sending deltas, would cut most of this traffic.

## 3. Perceived Latency

Time between the host's action (start, close, leaderboard, finish) and the moment the new state reaches the player. 26 actions x 15 players = 390 measures per approach.

| Approach        | Min    | Median      | Mean        | p95         | Max      |
| --------------- | ------ | ----------- | ----------- | ----------- | -------- |
| WebSocket       | 1.1 ms | **2.7 ms**  | 2.9 ms      | 4.5 ms      | 5.6 ms   |
| Polling (2 s)   | 16 ms  | **1,137 ms**| 1,048 ms    | 1,893 ms    | 2,001 ms |

With polling, the delay is the time until the player's next request: it is uniform between 0 and 2 s, so about 1 s on average and up to 2 s, plus one network round trip. With WebSockets the server pushes right away, so the delay is about half a round trip plus a few milliseconds of processing.

This matters for LeBuzzer because the score depends on speed: `points = 500 + 500 x remaining / duration`. On a 15-second question, a player who sees the question 2 s late can lose up to **67 points** before even reading it. On a 10-second question, up to 100 points. With polling, the ranking would partly depend on when each phone happens to poll.

## Conclusion

For LeBuzzer, WebSockets are the right choice:

- Latency goes from about 1 s (up to 2 s) to a few milliseconds, which keeps the speed-based scoring fair.
- No request is wasted: 2 out of 3 polling responses carried nothing new.
- Host actions are sent on the same connection, with no extra HTTP request.

But the measures also show that WebSockets are not free. Here they exchanged more messages than polling (6,279 vs 4,500), because of heartbeats and of the snapshot broadcast after every answer. The gain is in latency and useful traffic, not in raw message count. They also bring extra complexity: heartbeats, reconnection, session tracking on the server, and a stateful server that is harder to scale across several instances.

### When Polling Remains the Right Choice

- **A public "spectator" scoreboard**, for example the ranking displayed on a website for hundreds of viewers. A 2 s delay does not matter there, and the same `GET` response can be cached (CDN, `ETag` / `304 Not Modified`). The server answers once per interval instead of keeping hundreds of open connections.
- **Rare or non-critical updates**, such as checking whether a room still exists or listing the open rooms: one request from time to time is simpler than a connection kept alive with a heartbeat every 5 seconds.
- **Restrictive networks**: some corporate or school proxies block WebSockets. SockJS itself falls back to HTTP streaming and long polling in that case.
- **Stateless or serverless hosting**, where long-lived connections are not supported or are billed by the minute.

LeBuzzer already mixes both: it uses WebSockets for the live game, and a plain HTTP `GET /snapshot` to check a room before joining and to resynchronise after a reconnection.
