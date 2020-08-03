import java.io.{BufferedWriter, File, FileWriter}
import java.util
import java.text.SimpleDateFormat

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Date, Properties}

import utils.Constants
import play.api.libs.json.{JsArray, Json, Writes}

import scala.collection.JavaConverters._
import scala.util.parsing.json.JSONArray

object Consumer {
  def main(args: Array[String]): Unit = {
    //    consumeFromKafka(Constants.peopleTopic)
    consumeEventsFromKafka(Constants.eventsTopic)
  }

  def consumeFromKafka(topic: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", Constants.address)
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))
    var peopleList = new JsArray()

    val sdf = new SimpleDateFormat("ddMMyyyy_hhmmss")
    val curDate = new Date
    val strDate = sdf.format(curDate)
    val fileName = Constants.peopleJsonDir + "people" + strDate + ".json"
    val file = new File(fileName)

    try {
      while (true) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          val bw = new BufferedWriter(new FileWriter(file))
          val msg = data.value()
          val json = Json parse msg
          peopleList = json +: peopleList
          println(peopleList.toString())
          val firstName = (json \ "first_name").as[String]
          val lastName = (json \ "last_name").as[String]
          println("Name: " + firstName + " " + lastName)
          bw.write(peopleList.toString())
          bw.close()
        }
      }
    }
    finally {

    }
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
    var eventsList = new JsArray()

    val sdf = new SimpleDateFormat("ddMMyyyy_hhmmss")
    val curDate = new Date
    val strDate = sdf.format(curDate)
    val fileName = Constants.eventsJsonDir + "events" + strDate + ".json"
    val file = new File(fileName)

    try {
      while (true) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          val bw = new BufferedWriter(new FileWriter(file))
          val msg = data.value()
          val json = Json parse msg
          //          println("Commit message: " + (json\ "body" \ "commits" \ 0 \ "message" ).as[String])
          eventsList = json +: eventsList
          Json prettyPrint json
          bw.write(eventsList.toString())
          bw.close()
        }
      }
    }
    finally {

    }
  }
}

//  def consumeEventsFromKafka(topic: String) = {
//    val props = new Properties()
//    props.put("bootstrap.servers", "localhost:9092")
//    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
//    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
//    props.put("auto.offset.reset", "latest")
//    props.put("group.id", "consumer-group")
//    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
//    consumer.subscribe(util.Arrays.asList(topic))
//    while (true) {
//      val record = consumer.poll(1000).asScala
//      for (data <- record.iterator) {
//        val msg = data.value()
//        val json = Json parse msg
//        println("Commit message: " + (json\ "body" \ "commits" \ 0 \ "message" ).as[String])
//      }
//    }
//  }
//}