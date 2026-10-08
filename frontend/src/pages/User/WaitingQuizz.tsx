import { Navigate, useParams } from 'react-router'
import Header from '../../components/Header'
import { useGame } from '../../context/GameContext'
import '../../styles/WaitingQuizz.css'

export default function WaitingQuizz() {
  const { gameCode = '' } = useParams()
  const { session, snapshot } = useGame()

  if (!session || session.gameCode !== gameCode.toUpperCase()) {
    return <Navigate to={`/JoinQuizz/${gameCode}`} replace />
  }

  const playerCount = snapshot?.onlinePlayers.length ?? 0

  return (
    <main className="waiting">
      <Header />

      <div className="waiting-content">
        <h1 className="page-title">You're in!</h1>

        <div className="waiting-card">
          <span className="waiting-nickname">{session.nickname}</span>
          <span className="waiting-room">Room {session.gameCode}</span>
        </div>

        <p className="waiting-status" aria-live="polite">
          Waiting for the host to start
          <span className="waiting-dots" aria-hidden="true">
            <span>.</span><span>.</span><span>.</span>
          </span>
        </p>

        <p className="waiting-count">
          {playerCount} {playerCount === 1 ? 'player' : 'players'} online
        </p>
      </div>
    </main>
  )
}
