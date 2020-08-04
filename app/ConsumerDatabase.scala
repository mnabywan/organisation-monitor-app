import java.util.Properties
import java.util

import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI
import com.mongodb.util.JSON
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Date, Properties}

import utils.Constants
import play.api.libs.json.{JsArray, JsValue, Json, Writes}

import scala.collection.JavaConverters._
import scala.util.parsing.json.JSONArray
import utils.{Constants, MongoConfiguration}


object ConsumerDatabase{

  private val mongo_url = MongoClientURI(MongoConfiguration.mongoConnectionUrl)
  private val mongoClient: MongoClient = MongoClient(mongo_url)
  private val db = mongoClient(MongoConfiguration.database)
  private val eventsCollection = db(MongoConfiguration.eventsCollection)


  def main(args: Array[String]): Unit = {
    consumeEventsFromKafka(Constants.eventsTopic)
    //val job = MongoDBObject("name"-> spark, "status"-> "success")
  }

  def consumeEventsFromKafka(topic: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", Constants.address)
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")
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
          println("Number of events in collection: " + eventsCollection.count())
        }
      }
    }
    finally {

    }
  }


  def insertJsonToDb(json : JsValue): Unit ={
    val jsonString = json.toString()
    val dbObject: DBObject = JSON.parse(jsonString).asInstanceOf[DBObject]
    eventsCollection.insert(dbObject)
  }


}


