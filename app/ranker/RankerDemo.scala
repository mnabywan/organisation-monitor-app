package ranker

import org.mongodb.scala.model.Filters.and
import Helpers._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Accumulators, Aggregates, Filters}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

object RankerDemo {


  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("db")
  val collection: MongoCollection[Document] = database.getCollection("events")


  def main(args : Array[String]): Unit ={

      println(getCommentsNumber("mnabywan"))
  }

  def calculateRank(numberOfCommits : Int, numberOfAddedFiles :Int, numberOfModifiedFiles:Int, numberOfRemovedFiles: Int,
                    numberOfPullRequests :Int, numberOfComments : Int): Int ={
    var rank = 0: Int
      rank = numberOfCommits * 3 + numberOfPullRequests * 5 +
        (numberOfAddedFiles + numberOfModifiedFiles + numberOfRemovedFiles) * 1 + numberOfComments *2
    return rank
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
  def getCommitsNumberById(id: ObjectId) : Int = {
    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.equal("_id", id)),
      Aggregates.project(Document("username" -> "$body.pusher.name", "commits" -> "$body.commits")),
      Aggregates.unwind("$commits"),
      Aggregates.group("$username", Accumulators.sum("commits", 1))
    )).headResult()


    if (result == null) {
      return 0
    }
    else {
      println(result.getString("_id") + " " + result.getInteger("commits"))
      return (result.getInteger("commits"))
    }
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


  def getAddedFilesById(id : ObjectId): Int ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.equal("_id", id)),
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

  def getModifiedFilesById(id : ObjectId): Int ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.equal("id", id)),
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


  def getRemovedFilesById(id : ObjectId): Int ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.equal("_id", id)),
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

    println("Pull requests for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.pull_request", true ), Filters.equal("body.sender.login", username))),
      Aggregates.project(Document("username" -> "$body.sender.login", "commits" -> "$body.commits")),
      Aggregates.project(Document("username" -> "$body.sender.login", "pull_requests" -> "$body.pull_requests")),
       Aggregates.group("$username", Accumulators.sum("pull_request", 1))
    )).headResult()

    if (result == null){
      return 0
    }
    else {
      return(result.getInteger("pull_request"))
    }
  }

  def getPullRequestsNumberById(id: ObjectId) : Int ={

    var result = collection.aggregate(Seq(
      Aggregates.filter(Filters.equal("_id", id)),
      Aggregates.project(Document("username" -> "$body.sender.login", "commits" -> "$body.commits")),
      Aggregates.project(Document("username" -> "$body.sender.login", "pull_requests" -> "$body.pull_requests")),
      Aggregates.group("$username", Accumulators.sum("pull_request", 1))
    )).headResult()

    if (result == null){
      return 0
    }
    else {
      return(result.getInteger("pull_request"))
    }
  }


  def getCommentsNumber(username: String) : Int ={

    println("Comments for " + username)
    var result = collection.aggregate(Seq(
      Aggregates.filter(and(Filters.exists("body.comment", true ), Filters.equal("body.sender.login", username))),
      Aggregates.project(Document("username" -> "$body.sender.login", "comment" -> "$body.comment")),
      Aggregates.group("$username", Accumulators.sum("comment", 1))
    )).headResult()

    if (result == null){
      return 0
    }
    else {
      return(result.getInteger("comment"))
    }
  }




  //  def example() = {
//      collection.aggregate(Seq(
//          Aggregates.filter(Filters.exists("body.commits", true )),
//
//  }

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
