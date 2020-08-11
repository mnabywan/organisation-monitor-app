package controllers

import scalaj.http.{Http, HttpRequest, HttpResponse}
import play.api.libs.json._
import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import utils.Constants
import views._
import ranker.RankerDemo

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  private val producer = new Producer();
  private val endpoint = "https://api.pipedream.com/v1/sources/dc_gzuN4A/event_summaries?expand=event"
  private val authToken = "1797a52f2127c032e45f4a2fa613b7cc"


  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  val peopleForm = Form(
    tuple(
      "First name" -> nonEmptyText,
      "Last name" -> nonEmptyText
    )
  )


  def people() = Action{ implicit request =>
    Ok(views.html.people(peopleForm))
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }



  def newPerson = Action{ implicit request =>
    peopleForm.bindFromRequest.fold(
      errors => BadRequest(views.html.people(errors)),
      person =>{
        producer.writeToKafkaFromForm(Constants.peopleTopic, person)
        Redirect(routes.HomeController.people())
      }
    )
  }

  def newEvent = Action { request: Request[AnyContent] =>
    val body: AnyContent          = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody
      .map { json =>
        producer.writeEventToKafka(Constants.eventsTopic, json)
        Ok("Got: " + (json \ "method").as[String])
      }
      .getOrElse {
        BadRequest("Expecting application/json request body")
      }
  }


  def gitEvents = Action{
    val response: HttpResponse[String] = Http(endpoint).
      header("Authorization", "Bearer " + authToken).
      param("q", "monkeys").asString

    val events = Json parse response.body.toString
    Ok(views.html.events(events))
  }

  def commitsAll = Action{
    val commits = RankerDemo.getCommitsNumber()
    Ok(views.html.ranking(commits))
  }

//  def commits(username:String) = Action{
//    val commits = RankerDemo.getCommitsNumber(username)
//    Ok(views.html.ranking(commits))
//  }
}
