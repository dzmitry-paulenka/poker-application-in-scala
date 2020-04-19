package com.evo.poker

package object logic {
  type OrError[T] = Either[String, T]
}
