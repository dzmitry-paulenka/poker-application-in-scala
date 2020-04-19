package com.evo.poker.logic

sealed trait GameTransition

case object Deal                                extends GameTransition
case class Join(playerId: String, balance: Int) extends GameTransition
case class Leave(playerId: String)              extends GameTransition
case class Check(playerId: String)              extends GameTransition
case class Call(playerId: String)               extends GameTransition
case class Raise(playerId: String, amount: Int) extends GameTransition
case class Fold(playerId: String)               extends GameTransition
case object Finish                              extends GameTransition

object GameTransition {
  def start() = ???
}
