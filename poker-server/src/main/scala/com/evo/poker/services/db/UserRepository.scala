package com.evo.poker.services.db

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.{Document, MongoCollection}

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(userCollection: MongoCollection[UserEntity]) {
  implicit val executionContext = ExecutionContext.global

  def findById(id: String): Future[Option[UserEntity]] =
    userCollection
      .find(Document("_id" -> new ObjectId(id)))
      .first
      .head
      .map(Option(_))

  def findByUsername(username: String): Future[Option[UserEntity]] =
    userCollection
      .find(Document("username" -> username))
      .first
      .head
      .map(Option(_))

  def updateBalance(id: String, balance: Int): Future[Boolean] =
    userCollection
      .updateOne(
        Document("_id" -> new ObjectId(id)),
        Document("balance" -> balance)
      )
      .head
      .map { _.getModifiedCount > 0 }

  def create(user: UserEntity): Future[String] =
    userCollection
      .insertOne(user)
      .head
      .map { _ => user._id.toHexString }
}
