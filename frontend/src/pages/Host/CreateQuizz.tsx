import { useEffect, useRef, useState } from 'react'
import type { Client } from '@stomp/stompjs'
import { QRCodeSVG } from 'qrcode.react'
import Header from '../../components/Header'
import { createGame, getSnapshot, loadHostGame, saveHostGame } from '../../services/gameApi'
import { connectToGame, startNextQuestion } from '../../services/gameSocket'
import type { GameSnapshot, HostGame } from '../../types/game'
import '../../styles/CreateQuizz.css'

async function openRoom(): Promise<{ game: HostGame; snapshot: GameSnapshot | null }> {
  const saved = loadHostGame()
  if (saved) {
    const snapshot = await getSnapshot(saved.gameCode)
    if (snapshot?.state === 'WAITING') {
      return { game: saved, snapshot }
    }
  }
  const game = await createGame()
  saveHostGame(game)
  return { game, snapshot: await getSnapshot(game.gameCode) }
}

export default function CreateQuizz() {
  const [game, setGame] = useState<HostGame | null>(null)
  const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null)
  const [connected, setConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const roomRequested = useRef(false)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (roomRequested.current) return
    roomRequested.current = true

    openRoom()
      .then((room) => {
        setGame(room.game)
        setSnapshot(room.snapshot)
      })
      .catch(() => setError('Unable to create the room. Is the server running?'))
  }, [])

  useEffect(() => {
    if (!game) return

    const client = connectToGame(game.gameCode, {
      onSnapshot: setSnapshot,
      onConnectionChange: setConnected,
    })
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [game])

  const players = snapshot?.onlinePlayers ?? []
  const started = snapshot !== null && snapshot.state !== 'WAITING'
  const joinUrl = game ? `${window.location.origin}/JoinQuizz/${game.gameCode}` : ''

  function handleStart() {
    if (!game || !clientRef.current) return
    startNextQuestion(clientRef.current, game.gameCode, game.hostToken)
  }

  return (
    <main className="room">
      <Header />

      <div className="room-content">
        <h1 className="page-title room-title">Create a Room</h1>

        {error && <p className="room-error" role="alert">{error}</p>}

        {!error && !game && <p className="room-loading">Creating the room…</p>}

        {game && (
          <>
            <div className="room-code">
              <span className="room-code-label">Room code</span>
              <span className="room-code-value">{game.gameCode}</span>
            </div>

            <div className="room-qr">
              <QRCodeSVG
                value={joinUrl}
                size={256}
                bgColor="transparent"
                fgColor="currentColor"
                title={`Join room ${game.gameCode}`}
              />
            </div>

            <section className="room-players" aria-labelledby="room-players-title">
              <h2 id="room-players-title" className="visually-hidden">Players</h2>
              {players.length === 0 ? (
                <p className="room-players-empty">Waiting for players…</p>
              ) : (
                <ul className="room-players-list">
                  {players.map((player) => (
                    <li key={player} className="room-player">{player}</li>
                  ))}
                </ul>
              )}
            </section>

            <p className="room-count" aria-live="polite">
              {players.length} {players.length === 1 ? 'player' : 'players'} online
            </p>

            <button
              type="button"
              className="button room-start"
              onClick={handleStart}
              disabled={!connected || players.length === 0 || started}
            >
              {started ? 'Started' : 'Start'}
            </button>
          </>
        )}
      </div>
    </main>
  )
}
