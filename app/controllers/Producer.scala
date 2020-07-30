package controllers

import java.util.Properties

import org.apache.kafka.clients.producer._
import play.api.libs.json.JsValue

class Producer{
  def main(args: Array[String]): Unit = {
    writeToKafka("people4")
  }

  def writeToKafka(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val json =
      """{
        |"first_name" : "Mateusz",
        |"last_name" : "Nowak"
        |}
        |""".stripMargin

    val msg = new ProducerRecord[String, String ](topic, "key", json)
    producer.send(msg)
    producer.close()
  }


  def writeToKafkaFromForm(topic: String, person :(String, String)): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val jsonString =
      """{
        |"first_name" : "%s",
        |"last_name" : "%s"
        |}
        |""".format(person._1, person._2).stripMargin

    val msg = new ProducerRecord[String, String ](topic, "key", jsonString)
    producer.send(msg)
    producer.close()
  }

  def writeEventToKafka(topic : String, event:JsValue):Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val msg = new ProducerRecord[String, String](topic, "key", event.toString())
    producer.send(msg)
    producer.close()
  }
}