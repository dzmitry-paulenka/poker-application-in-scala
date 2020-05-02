package com.evo.poker.services.db

import org.bson.types.ObjectId

case class UserEntity(_id: ObjectId, username: String, passwordHash: String, balance: Int)
