package auth

import javax.inject.Inject
import pdi.jwt._
import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


case class UserRequest[A](jwt: JwtClaim, token: String, request: Request[A]) extends WrappedRequest[A](request)

class AuthAction @Inject()(bodyParser: BodyParsers.Default, authService: AuthService)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  private val headerTokenRegex = """Bearer (.+?)""".r


  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
    extractBearerToken(request) map { token =>
      authService.validateJwt(token) match {
        case Success(claim) => block(UserRequest(claim, token, request))      // token was valid - proceed!
        case Failure(t) => Future.successful(Results.Unauthorized(t.getMessage))  // token was invalid - return 401
      }
    } getOrElse Future.successful(Results.Unauthorized)     // no token was sent - return 401

  private def extractBearerToken[A](request: Request[A]): Option[String] = {
        request.headers.get(HeaderNames.AUTHORIZATION) collect {
          case headerTokenRegex(token) => token
        }
//    val token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ilp4QkVYUUJiQ1FIX1FjYnpHQktnQSJ9.eyJpc3MiOiJodHRwczovL3NjYWxhLWFwaS5ldS5hdXRoMC5jb20vIiwic3ViIjoicFhLYW5OQnZMU3A3N0RQN2NFakltWkhhZHB0V1dyQmVAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vbXktc2NhbGEtYXBpLmV4YW1wbGUuY29tIiwiaWF0IjoxNTk4NDM5MTMyLCJleHAiOjE1OTg1MjU1MzIsImF6cCI6InBYS2FuTkJ2TFNwNzdEUDdjRWpJbVpIYWRwdFdXckJlIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.DAaCaBep1suhYe5SneRopqjRpS6ayiipGTvcVYsqEEyE2YbkjbEFsM6gfWjiBRYHAw41oQBsf1hB0-yGM21wXZ2umQ86TN_npgh-47C91ytmyQ34QLU3fGLjlCy92D0SJGWYHVUy_88YPrDigFUxN5w-9XwgzGuLzzaXxkbQrCadwRv1conCVBRpoa8aOAaxzKMuHGEJQ4_fFw-l4Mc2crYvYuvzuo8O5cS1gLsrt6S3NU1aIRtuJpPqEqEz-B0FDtlNraQ0HH0w7QAIDEwBMQ9bUhO_5S3wo84zqB8wOFLaqbAt9H2GDy01w0p4jbhWxAR2evdUB9DbB8eGotYVLQ"
//    Option(token)
  }
}