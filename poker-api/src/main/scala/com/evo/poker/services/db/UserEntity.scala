package com.evo.poker.services.db

import org.bson.types.ObjectId

final case class UserEntity(_id: ObjectId, username: String, passwordHash: String, balance: Int)
