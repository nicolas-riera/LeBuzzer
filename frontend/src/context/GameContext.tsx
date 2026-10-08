import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import type { Client } from '@stomp/stompjs'
import { getSnapshot } from '../services/gameApi'
import { connectToGame, joinGame } from '../services/gameSocket'
import type { GameSnapshot, PlayerSession } from '../types/game'

const JOIN_TIMEOUT_MS = 5000

interface GameContextValue {
  session: PlayerSession | null
  snapshot: GameSnapshot | null
  join: (gameCode: string, nickname: string) => Promise<void>
  leave: () => void
}

const GameContext = createContext<GameContextValue | null>(null)

export function GameProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<PlayerSession | null>(null)
  const [snapshot, setSnapshot] = useState<GameSnapshot | null>(null)
  const clientRef = useRef<Client | null>(null)

  const leave = useCallback(() => {
    clientRef.current?.deactivate()
    clientRef.current = null
    setSession(null)
    setSnapshot(null)
  }, [])

  const join = useCallback(async (gameCode: string, nickname: string) => {
    const code = gameCode.trim().toUpperCase()
    const name = nickname.trim()

    const current = await getSnapshot(code)
    if (!current) {
      throw new Error('This room does not exist.')
    }
    if (current.state === 'FINISHED') {
      throw new Error('This game is already over.')
    }
    if (current.onlinePlayers.some((player) => player.toLowerCase() === name.toLowerCase())) {
      throw new Error('This nickname is already taken.')
    }

    leave()
    setSnapshot(current)

    await new Promise<void>((resolve, reject) => {
      let joined = false
      const timeout = setTimeout(() => {
        client.deactivate()
        clientRef.current = null
        reject(new Error('Unable to join the room. Please try again.'))
      }, JOIN_TIMEOUT_MS)

      const client = connectToGame(code, {
        onSnapshot: setSnapshot,
        onPlayerJoined: (player) => {
          if (joined || player.id.toLowerCase() !== name.toLowerCase()) return
          joined = true
          clearTimeout(timeout)
          setSession({ gameCode: code, nickname: player.id })
          resolve()
        },
        onConnectionChange: (connected) => {
          if (connected) joinGame(client, code, name)
        },
      })
      clientRef.current = client
    })
  }, [leave])

  useEffect(() => () => {
    clientRef.current?.deactivate()
  }, [])

  return (
    <GameContext.Provider value={{ session, snapshot, join, leave }}>
      {children}
    </GameContext.Provider>
  )
}

export function useGame() {
  const context = useContext(GameContext)
  if (!context) {
    throw new Error('useGame must be used inside a GameProvider')
  }
  return context
}
