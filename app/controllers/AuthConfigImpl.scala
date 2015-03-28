package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import models.{Account, NormalUser, Role}
import play.api.mvc.RequestHeader
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}


trait AuthConfigImpl extends AuthConfig with Controller{

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = String

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = Account

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Role
   * case object Administrator extends Role
   * case object NormalUser extends Role
   */
  type Authority = Role

  /**
   * A `ClassTag` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag: ClassTag[Id] = classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Account.findAccountByUsername(id)

  /**
   * Where to redirect the user after a successful login.
   * Called from 'gotoLoginSucceeded'
   */
  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.dashboard()))

  /**
   * Where to redirect the user after logging out
   */
  //TODO
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index()))

  /**
   * If the user is not logged in and tries to access a protected resource then redirct them as follows:
   */
  //TODO log data when authentication fails (developer version)
  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.IdentificationController.signin()))

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Forbidden("no permission"))

  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user.Role, authority) match {
      case ("NormalUser", NormalUser) => true
      case _                        => false
    }
  }

  /**
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
   */
  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

  /**
   * Whether a login session is closed when the brower is terminated.
   * default is false.
   */
  override lazy val isTransientCookie: Boolean = false
}
