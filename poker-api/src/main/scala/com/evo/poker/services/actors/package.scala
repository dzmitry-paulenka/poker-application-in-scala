package com.evo.poker.services

import com.evo.poker.logic.Player

package object actors {
  implicit class PlayerActorOpts(p: Player) {
    // cutting corners here - normally we need an actual metadata to determine a bot player
    lazy val isBot: Boolean =
      p.id.startsWith("BOT-[")
  }
}
