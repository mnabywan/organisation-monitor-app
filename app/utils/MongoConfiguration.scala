package utils

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

object MongoConfiguration {
  val mongoConnectionUrl = "mongodb://localhost:27017"
  val database = "db"
  val eventsCollection = "events"

}
