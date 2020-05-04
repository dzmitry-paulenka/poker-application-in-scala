package com.evo.poker.logic

sealed trait GameTransition
case object Deal      extends GameTransition
case object NextRound extends GameTransition
case object End       extends GameTransition

sealed trait PlayerTransition extends GameTransition {
  val playerId: String
}
final case class Join(playerId: String)               extends PlayerTransition
final case class Leave(playerId: String)              extends PlayerTransition
final case class Check(playerId: String)              extends PlayerTransition
final case class Call(playerId: String)               extends PlayerTransition
final case class Raise(playerId: String, amount: Int) extends PlayerTransition
final case class Fold(playerId: String)               extends PlayerTransition
