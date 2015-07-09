import java.util.Calendar
import actors.{WebSocketsActor, TestActor}
import akka.actor.Props
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc.Results._
import play.api.mvc._
import scala.concurrent.Future
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started: " + Calendar.getInstance().getTime)

    val testActor = Akka.system.actorOf(Props[TestActor],"testActor")
    val webSocketsActor = Akka.system.actorOf(Props[WebSocketsActor], "webSocketsActor")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown: " + Calendar.getInstance().getTime)
  }

  // called when a route is found, but it was not possible to bind the request parameters
  override def onBadRequest(request: RequestHeader, error: String) = {
    Future(Redirect("/error/404"))
    //Future(BadRequest("Bad Request: " + error))
  }

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = {
    Future(Redirect("/error/500"))
    //Future(InternalServerError(views.html.Errors.onError(throwable)))
  }

  // 404 - page not found error
  override def onHandlerNotFound(request: RequestHeader) = {
    //Future(NotFound(views.html.Errors.onHandlerNotFound(request)))
    Future(Redirect("/error/404"))
  }

}
