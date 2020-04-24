package com.evo.poker.services

package object actors {

  case class ErrorMessage(error: String)

  case class ActiveGame(
    id: String,
    name: String,
    smallBlind: Int,
    buyIn: Int,
    playersCount: Int
  )
}
