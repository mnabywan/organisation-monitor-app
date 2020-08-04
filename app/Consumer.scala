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

  val eventsFile = "events"
  val peopleFile = "people"

  def main(args: Array[String]): Unit = {
//    consumeFromKafka(Constants.peopleTopic)
    consumeEventsFromKafka(Constants.eventsTopic)
  }


  def getFile(dir : String, fileName:String, fileNum :Int) :File = {
    var file = new File(dir + fileName + "_" + fileNum.toString + ".json")

    var size = file.length()
    var fileNumber = fileNum
    while(size > Constants.maxFileSize){
      fileNumber +=1
      file = new File(dir + fileName + "_" + fileNumber.toString + ".json")
      size = file.length
    }
    println(file.getName)
    file
  }

  def getNextFile(dir : String, fileName : String, fileNum :Int): File={
    var fileNumber = fileNum + 1
    var file = new File(dir + fileName + "_" + fileNumber.toString + ".json")
    file
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
    var fileNum = 1
    var file = getFile(Constants.peopleJsonDir, peopleFile, fileNum)

    try {
      while (true) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          if (file.length > Constants.maxFileSize){
            println("Get next file")
            file = getNextFile(Constants.eventsJsonDir, peopleFile, fileNum)
            fileNum += 1
          }
          val bw = new BufferedWriter(new FileWriter(file, true))
          val msg = data.value()
          val json = Json parse msg

          val firstName = (json \ "first_name").as[String]
          val lastName = (json \ "last_name").as[String]
          println("Name: " + firstName + " " + lastName)
          bw.write(json.toString())
          bw.write("\n")
          bw.close()
          println(file.length())
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
    var fileNum = 1
    var file = getFile(Constants.eventsJsonDir, eventsFile, fileNum)


    try {
      while (true) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          if (file.length > Constants.maxFileSize){
            println("Get next file")
            file = getNextFile(Constants.eventsJsonDir, eventsFile, fileNum)
            fileNum += 1
          }
          val bw = new BufferedWriter(new FileWriter(file, true))
          val msg = data.value()
          val json = Json parse msg
          //          println("Commit message: " + (json\ "body" \ "commits" \ 0 \ "message" ).as[String])
          Json prettyPrint json
          bw.write(json.toString())
          bw.write("\n")
          bw.close()
          println(file.length())
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