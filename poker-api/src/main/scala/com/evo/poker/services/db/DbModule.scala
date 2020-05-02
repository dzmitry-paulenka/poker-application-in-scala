package com.evo.poker.services.db

import java.util.concurrent.TimeUnit

import com.softwaremill.macwire.{Module, wire}
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.{IndexOptions, Indexes}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import com.evo.poker.services.config.ConfigModule

@Module
trait DbModule extends ConfigModule {

  lazy val codecRegistry = fromRegistries(
    fromProviders(classOf[UserEntity]),
    MongoClient.DEFAULT_CODEC_REGISTRY
  )

  lazy val mongoClient = MongoClient(config.getString("mongo.url"))

  lazy val mongoDb: MongoDatabase = mongoClient
    .getDatabase(config.getString("mongo.database"))
    .withCodecRegistry(codecRegistry)

  lazy val userCollection: MongoCollection[UserEntity] = mongoDb.getCollection[UserEntity]("users")

  Await.result(
    userCollection.createIndex(Indexes.ascending("username"), IndexOptions().unique(true)).toFuture(),
    Duration(10, TimeUnit.SECONDS)
  )

  lazy val userRepository = wire[UserRepository]
}
