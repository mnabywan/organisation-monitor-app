import java.util

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.Properties

import play.api.libs.json.Json

import scala.collection.JavaConverters._

object Consumer{
  def main(args: Array[String]): Unit = {
//    consumeFromKafka("people4")
    consumeEventsFromKafka("events2")
  }

  def consumeFromKafka(topic: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))
    while (true) {
      val record = consumer.poll(1000).asScala
      for (data <- record.iterator) {
        val msg = data.value()
        val json = Json parse msg
        val firstName = (json \ "first_name").as[String]
        val lastName = (json \ "last_name").as[String]
        println("Name: "  + firstName + " " + lastName)
      }
    }
  }


  def consumeEventsFromKafka(topic: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))
    while (true) {
      val record = consumer.poll(1000).asScala
      for (data <- record.iterator) {
        val msg = data.value()
        val json = Json parse msg
        println("Commit message: " + (json\ "body" \ "commits" \ 0 \ "message" ).as[String])

      }
    }
  }
}