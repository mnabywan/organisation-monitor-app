import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import utils.Constants
import java.util.Properties
import java.util

import com.mongodb.DBObject
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Accumulators, Aggregates, Filters, Sorts, Updates}
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer}
import play.api.libs.json.{JsValue, Json}
import ranker.RankerDemo
import ranker.Helpers._

import scala.util.parsing.json.JSON
//
//import com.mongodb.DBObject
//import com.mongodb.casbah.MongoClient
//import com.mongodb.casbah.MongoClientURI
//import org.apache.kafka.clients.consumer.KafkaConsumer
//import java.io.{BufferedWriter, File, FileWriter}
//import java.text.SimpleDateFormat
//
//import org.apache.kafka.clients.consumer.KafkaConsumer
////import java.util.{Date, Properties}
//
//import com.mongodb.casbah.commons.MongoDBObject
//import utils.Constants
import play.api.libs.json.{JsArray, JsValue, Json, Writes}
//
import scala.collection.JavaConverters._
import scala.util.parsing.json.JSONArray
import utils.{Constants, MongoConfiguration}
//

object ConsumerDatabase{

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("db")
  val collection: MongoCollection[Document] = database.getCollection("events")
  val usersCollection : MongoCollection[Document] = database.getCollection("users")


  def main(args: Array[String]): Unit = {
    consumeEventsFromKafka(Constants.eventsTopic)

//    example("ssaaaaaaaaaa")
  }

  def example(user:String):Unit={
    val searchUser = usersCollection.find(Filters.equal("_id", user)).headResult()
    println(searchUser)
    println(searchUser == null)
    val doc : Document = Document("_id" -> user, "total_cfaas" -> 190)
    val observable: Observable[UpdateResult] = usersCollection.replaceOne(Filters.equal("_id", user), doc)
    observable.subscribe(new Observer[UpdateResult] {
      override def onNext(result: UpdateResult): Unit = println("Updated")
      override def onError(e: Throwable): Unit = println("Failed")
      override def onComplete(): Unit = println("Completed")
    })

  }

  def getCommitsNumberById(id: ObjectId) : Int = {
    var result = collection.aggregate(Seq(
      Aggregates.filter( Filters.equal("_id", id)),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.group("$username", Accumulators.sum("commits", 1))
    )).headResult()


    if (result == null){
      return 0
    }
    else {
      println(result.getString("_id") + " " + result.getInteger("commits"))
      return(result.getInteger("commits"))
    }
  }

  def consumeEventsFromKafka(topic: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", Constants.address)
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group2")
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    try {
      while (true) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          val msg = data.value()
          val json = Json parse msg
          Json prettyPrint json
          insertJsonToDb(json)
          println("Number of events in collection: " + collection.countDocuments().toString)
        }
      }
    }
    finally {

    }
  }


  def insertJsonToDb(json : JsValue): Unit ={
    var inserted : Boolean = false
    val jsonString = json.toString()
    val doc = Document.apply(jsonString)
    println(doc.get("body"))

    val observable: Observable[Completed] = collection.insertOne(Document.apply(jsonString))

    observable.subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = { inserted= true; println("Inserted")}
      override def onError(e: Throwable): Unit = {inserted = false; println("Failed")}
      override def onComplete(): Unit = println("Completed")
    })


    if (inserted) {
      var numberOfCommits = 0
      var numberOfAddedFiles = 0
      var numberOfModifiedFiles = 0
      var numberOfRemovedFiles = 0
      var numberOfPullRequests = 0

      var result = collection.aggregate(Seq(
        Aggregates.project(Document("id" -> "$_id", "username" -> "$body.sender.login", "commits" -> "$body.commits", "pull_request" -> "$body.pull_request")),
        Aggregates.sort(Sorts.orderBy(Sorts.descending("id"))
        ))).headResult()
      println(result)
      var id = result.getObjectId("_id")
      var user = result.getString("username")
      println(result.get("commits"))
      println(user)
      println(id)

      if (result.get("commits") != None) {
        println("Get commtis")
        numberOfCommits = RankerDemo.getCommitsNumberById(id)
        numberOfAddedFiles = RankerDemo.getAddedFilesById(id)
        numberOfModifiedFiles = RankerDemo.getModifiedFilesById(id)
        numberOfRemovedFiles = RankerDemo.getRemovedFilesById(id)
      }
      else if (result.get("pull_request") != None) {
        println("Get pull requests")
        numberOfPullRequests = RankerDemo.getPullRequestsNumberById(id)
      }


      val onCompleteUpdate = new Observer[UpdateResult] {
        override def onNext(result: UpdateResult): Unit = println("Updated user. Result: " + result.toString)
        override def onError(e: Throwable): Unit = {
          println("ERROR: " + e.toString)
        }
        override def onComplete(): Unit = {
          println("complete")
        }
      }


      val searchUser = usersCollection.find(Filters.equal("_id", user)).headResult()
      if (searchUser == null){
        val doc : Document = Document("_id" -> user)
        usersCollection.insertOne(doc)
      }
      usersCollection.updateOne(
        Filters.equal("_id" , user),
        Updates.inc("total_additions", numberOfAddedFiles)
      )
        .subscribe(onCompleteUpdate)
      println("end")
    }

  }


}


