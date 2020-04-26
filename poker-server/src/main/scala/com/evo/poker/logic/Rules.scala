package com.evo.poker.logic

sealed abstract case class Rules private (
  handSize: Int,
  minHandUse: Int,
  maxHandUse: Int,
  boardSize: Int,
  playersLimit: Int,
  smallBlind: Int,
  buyIn: Int
) {
  val bigBlind: Int = smallBlind * 2
}

object Rules {

  def texas(smallBlind: Int = 1, buyIn: Int = 20): Rules = {
    of(2, 0, 2, smallBlind = smallBlind, buyIn = buyIn)
  }

  private def of(
    handSize: Int,
    minHandUse: Int,
    maxHandUse: Int,
    boardSize: Int = 5,
    playersLimit: Int = 9,
    smallBlind: Int = 10,
    buyIn: Int = 200
  ): Rules = {
    require(minHandUse + boardSize >= 5)

    require(minHandUse <= 5)
    require(maxHandUse <= 5)
    require(maxHandUse >= minHandUse)

    require(handSize >= minHandUse)
    require(handSize >= maxHandUse)

    require(buyIn >= smallBlind * 10)

    new Rules(handSize, minHandUse, maxHandUse, boardSize, playersLimit, smallBlind, buyIn) {}
  }
}
