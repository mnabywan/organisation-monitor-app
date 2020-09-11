package controllers

import play.api.data._
import play.api.data.Forms._

case class LoggingForm(username: String, password: String)


object LoggingForm {
  val form: Form[LoggingForm] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoggingForm.apply)(LoggingForm.unapply)
  )
}