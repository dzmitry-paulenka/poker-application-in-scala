package com.evo.poker.util

object Util {
  def ift[T](condition: Boolean, ifTrue: => T, ifFalse: => T) = {
    if (condition)
      ifTrue
    else
      ifFalse
  }
}
