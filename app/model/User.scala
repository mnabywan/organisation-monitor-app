package model
import utils.Constants

class User(var name : String, var numberOfCommits:Int, var numberOfPullRequests :Int,
           var numberOfAddedFiles: Int, numberOfModifiedFiles: Int, numberOfRemovedFiles: Int) {

    private var score = 0 :Int
    score = numberOfCommits * 3 + numberOfPullRequests * 5 +
              (numberOfAddedFiles + numberOfModifiedFiles + numberOfRemovedFiles) * 1

    def this(name:String) ={
      this(name, 0,0,0,0,0)
    }

    override def toString = s"User: name: $name score: $score"



}


