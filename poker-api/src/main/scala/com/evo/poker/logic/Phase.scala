package com.evo.poker.logic

sealed trait Phase

case object PreDeal  extends Phase
case object PreFlop  extends Phase
case object Flop     extends Phase
case object Turn     extends Phase
case object River    extends Phase
case object Showdown extends Phase
case object Ended    extends Phase

object Phase {
  def betsAllowed(phase: Phase): Boolean =
    phase == PreFlop || phase == Flop || phase == Turn || phase == River

  def cardsToDeal(phase: Phase): Int =
    phase match {
      case PreDeal  => 0
      case PreFlop  => 0
      case Flop     => 3
      case Turn     => 1
      case River    => 1
      case Showdown => 0
      case Ended    => 0
    }

  def nextPlayable(current: Phase): Phase =
    current match {
      case PreDeal  => PreFlop
      case PreFlop  => Flop
      case Flop     => Turn
      case Turn     => River
      case River    => Showdown
      case Showdown => PreDeal
      case Ended    => Ended
    }
}
