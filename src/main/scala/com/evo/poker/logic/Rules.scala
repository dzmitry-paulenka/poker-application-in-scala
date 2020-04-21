package com.evo.poker.logic

sealed abstract case class Rules private (
  handSize: Int,
  minHandUse: Int,
  maxHandUse: Int,
  boardSize: Int,
  smallBlind: Int = 10
) {
  val bigBlind       = smallBlind * 2
  val minimumBalance = smallBlind * 20
}

object Rules {
  val Texas: Rules = Rules.texas(1)
  val Omaha: Rules = Rules.omaha(1)

  def texas(smallBlind: Int) = {
    of(2, 0, 2, smallBlind = smallBlind)
  }

  def omaha(smallBlind: Int) = {
    of(4, 2, 2, smallBlind = smallBlind)
  }

  private def of(
    handSize: Int,
    minHandUse: Int,
    maxHandUse: Int,
    boardSize: Int = 5,
    smallBlind: Int
  ): Rules = {
    require(minHandUse + boardSize >= 5)

    require(minHandUse <= 5)
    require(maxHandUse <= 5)
    require(maxHandUse >= minHandUse)

    require(handSize >= minHandUse)
    require(handSize >= maxHandUse)

    new Rules(handSize, minHandUse, maxHandUse, boardSize, smallBlind) {}
  }
}
