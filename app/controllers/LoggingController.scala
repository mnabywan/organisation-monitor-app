package controllers

import auth.AuthConfiguration
import javax.inject._
import model.{SessionDAO}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import scalaj.http.{Http, HttpOptions, HttpResponse}
import play.api.mvc._


@Singleton
class LoggingController @Inject()(cc: ControllerComponents) extends AbstractController(cc)
  with play.api.i18n.I18nSupport{


  def loggingFormPost() = Action { implicit request: Request[AnyContent] =>
    val formData: LoggingForm = LoggingForm.form.bindFromRequest.get
    val username = formData.username
    val password = formData.password
    //      Redirect(routes.HomeController.index()).withSession(request.session + ("sessionToken" -> token))

    val response = Http("https://scala-api.eu.auth0.com/oauth/token").postData(
      f"""
         |{
         |        "grant_type" : "password",
         |        "client_id" : "pXKanNBvLSp77DP7cEjImZHadptWWrBe",
         |        "client_secret" : "BEJtOZOIyAr2DkMgjYkfvNB-iHhRr_zOXYQxJiJGj27YROHDPLTAbbFGzZC6HIlk",
         |        "audience" : \"${AuthConfiguration.authAudience}\",
         |        "username" : \"$username\",
         |        "password" : \"$password\",
         |        "scope" : "openid"
         |      }
         |""".stripMargin).
      header("content-type", "application/json").asString
    val json = Json parse response.body.toString
    val code = response.code
    code match {
      case 200 => {
        val token = (json \ "access_token").as[String]
        SessionDAO.generateToken(username, token)
        Redirect(routes.HomeController.priv()).withSession(request.session + ("sessionToken" -> token) +("username" -> username))
        //          Ok(code.toString + "  \n \n\n              " + )
      }
      case _ => Ok("dupa")
    }


  }

  //  def loggingFormPost() = Action { implicit request =>
  //    val formData: LoggingForm = LoggingForm.form.bindFromRequest.get // Careful: BasicForm.form.bindFromRequest returns an Option
  //    val password = formData.password
  //    Ok(password) // just returning the data because it's an example :)
  //  }

  def login() = Action{ implicit  request =>
    Ok(views.html.logging(LoggingForm.form))
  }

}