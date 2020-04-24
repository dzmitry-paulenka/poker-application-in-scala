package com.evo.poker.logic

sealed trait GameTransition
case object Deal   extends GameTransition
case object Finish extends GameTransition

sealed case class PlayerTransition(playerId: String) extends GameTransition
case class Join(override val playerId: String, balance: Int) extends PlayerTransition(playerId)
case class Leave(override val playerId: String)              extends PlayerTransition(playerId)
case class Check(override val playerId: String)              extends PlayerTransition(playerId)
case class Call(override val playerId: String)               extends PlayerTransition(playerId)
case class Raise(override val playerId: String, amount: Int) extends PlayerTransition(playerId)
case class Fold(override val playerId: String)               extends PlayerTransition(playerId)
