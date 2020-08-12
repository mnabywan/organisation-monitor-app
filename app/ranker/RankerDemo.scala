package ranker

import org.mongodb.scala.model.Filters.and
import Helpers._
import org.mongodb.scala.model.{Accumulators, Aggregates, Filters}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

object RankerDemo {


  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("db")
  val collection: MongoCollection[Document] = database.getCollection("events")


  def main(args : Array[String]): Unit ={

//    getAddedFiles("mnabywan")
//    getModifiedFiles("mnabywan")
//    getRemovedFiles("differentuser")
//    getAddedFiles()
      println(getPullRequestsNumber("mnabywan"))
//      getCommitsNumber()
  }

  def getCommitsNumber(username: String) : Int = {
    println("Added files for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.commits", true ), Filters.equal("body.pusher.name", username))),
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

  def getCommitsNumber() : String = {
    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.exists("body.commits", true )),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.group("$username", Accumulators.sum("commits", 1))
    )).results()

    var res = jsonizeDocs(result)
    println(res)
    res

  }

  def jsonizeDocs(cDocument: Seq[Document]): String = {
    val sb=new StringBuilder
    for (doc <- cDocument) {
      if (sb.nonEmpty) {
        sb.append(",")
      }
      sb.append(doc.toJson)
    }
    sb.toString
  }

  def getAddedFiles(username : String): Int ={

    println("Added files for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.commits", true ), Filters.equal("body.pusher.name", username))),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1, "commits"-> 1, "added_files" -> "$commits.added", "modified_files" -> "$commits.modified", "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$added_files"),
      Aggregates.group("$username",  Accumulators.sum( "added_files" ,1))
    )).headResult()


    if (result == null){
      return 0
    }
    else {
      return(result.getInteger("added_files"))
    }

    }


  def getAddedFiles(): Unit ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.exists("body.commits", true )),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1, "commits"-> 1, "added_files" -> "$commits.added", "modified_files" -> "$commits.modified", "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$added_files"),
      Aggregates.group("$username",  Accumulators.sum( "added_files" ,1))
    )).results

    for(res <- result){
      println(res.getString("_id") + " " + res.getInteger("added_files", 0))
    }

  }


  def getModifiedFiles(username : String): Int ={

    println("Modified files for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.commits", true ), Filters.equal("body.pusher.name", username))),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1, "commits"-> 1, "added_files" -> "$commits.added", "modified_files" -> "$commits.modified", "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$modified_files"),
      Aggregates.group("$username",  Accumulators.sum( "modified_files" ,1))
    )).headResult()


    if (result == null){
      return 0
    }
    else {
      return result.getInteger("modified_files")
    }

  }

  def getModifiedFiles(): Unit ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.exists("body.commits", true )),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1, "commits"-> 1, "added_files" -> "$commits.added", "modified_files" -> "$commits.modified", "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$added_files"),
      Aggregates.group("$username",  Accumulators.sum( "modified_files" ,1))
    )).results

    for(res <- result){
      println(res.getString("_id") + " " + res.getInteger("modified_files", 0))
    }

  }



  def getRemovedFiles(username : String): Int ={

    println("Removed files for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.commits", true ), Filters.equal("body.pusher.name", username))),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1,  "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$removed_files"),
      Aggregates.group("$username",  Accumulators.sum( "removed_files" ,1))
    )).headResult()

    if (result == null){
      return 0
    }
    else {
      return result.getInteger("removed_files")
    }
  }


  def getRemovedFiles(): Unit ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.exists("body.commits", true )),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.project(Document("username" -> 1, "commits"-> 1, "added_files" -> "$commits.added", "modified_files" -> "$commits.modified", "removed_files" ->"$commits.removed")),
      Aggregates.unwind("$added_files"),
      Aggregates.group("$username",  Accumulators.sum( "modified_files" ,1))
    )).results

    for(res <- result){
      println(res.getString("_id") + " " + res.getInteger("modified_files", 0))
    }

  }

  def getPullRequestsNumber(username: String) : Int ={

    println("Removed files for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.pull_request", true ), Filters.equal("body.pusher.name", username))),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.project(Document("username" -> "$body.pusher.name", "pull_requests" -> "$body.pull_requests")),
       Aggregates.group("$username", Accumulators.sum("pull_request", 1))
    )).headResult()

    if (result == null){
      return 0
    }
    else {
      return(result.getInteger("pull_request"))
    }
  }


//    collection.aggregate(Seq(
//      Aggregates.filter(Filters.exists("body.commits", true )),
//      Aggregates.group("$body.pusher.name", Accumulators.sum( "sumId", size("$body.commits", 1 )))
//    )).printResults()
//
//    println("Show commits")
//
//    collection.aggregate(Seq(
//      Aggregates.filter(Filters.exists("body.commits", true )),
//      group(
//        Document( "_id" -> "$body.pusher.name" ,
//          "commits" -> "$body.commits")
//      )
//    )).printResults()
//
//
//    collection.aggregate(Seq(
//      Aggregates.filter(Filters.exists("body.commits", true )),
//      Aggregates.group("$body.pusher.name"
//      )
//    )).printResults()



//    for (a <-v){
//      println(a)
//    }


}
