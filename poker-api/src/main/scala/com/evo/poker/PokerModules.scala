package com.evo.poker

import com.softwaremill.macwire.Module

import com.evo.poker.services.actors.ActorsModule
import com.evo.poker.services.db.DbModule
import com.evo.poker.services.http.HttpModule

@Module
class PokerModules extends HttpModule with ActorsModule with DbModule {}
