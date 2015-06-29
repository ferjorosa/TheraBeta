package controllers

import controllers.DeviceController._
import jp.t2v.lab.play2.auth.LoginLogout
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.{Success, Failure}

object IdentificationController extends AuthConfigImpl with LoginLogout {

  //Form-mapping used in the login action(only needs identifier (username/email) and password)
  val userLoginForm: Form[UserLogin] = Form(mapping(
    "Identifier" -> text(minLength = 3, maxLength = 20),
    "Password" -> text(minLength = 3, maxLength = 20)
  )(UserLogin.apply)(UserLogin.unapply))

  //Form-mapping used in the register action (needs all values)
  val userRegisterForm: Form[Account] = Form(mapping(
    "Username" -> text(minLength = 3, maxLength = 20),
    "Email" -> email,
    // Create a tuple mapping for the password/confirm
    // (We can do the same creating 2 properties in the model and then verifying it after 'User.unapply')
    "Password" -> tuple(
      "main" -> text(minLength = 6, maxLength = 20), //just in case
      "confirm" -> text(minLength = 6, maxLength = 20) //just in case
    ).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", passwords => passwords._1 == passwords._2
        //We need to transform the Tuple (String, String) into a String to match the User properties
      ).transform[String]({ passwords => passwords._1}, { p => p -> p}),
    "RealName" -> text(minLength = 3, maxLength = 32),
    "Country" -> text(minLength = 2, maxLength = 32),
    "PhoneNumber" -> text.verifying(maxLength(18), minLength(9), pattern( """[0-9.+]+""".r, error = Messages("register.phoneError"))),
    "Role" -> ignored(NormalUser.value)
  )(Account.apply)(Account.unapply))

  //Form-mapping used in the update profile action
  val updateProfileForm: Form[Account] = Form(mapping(
    "Username" -> ignored("profile_update"),
    "Email" -> email,
    // Create a tuple mapping for the password/confirm
    // (We can do the same creating 2 properties in the model and then verifying it after 'User.unapply')
    "Password" -> tuple(
      "main" -> text(minLength = 6, maxLength = 20), //just in case
      "confirm" -> text(minLength = 6, maxLength = 20) //just in case
    ).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", passwords => passwords._1 == passwords._2
        //We need to transform the Tuple (String, String) into a String to match the User properties
      ).transform[String]({ passwords => passwords._1}, { p => p -> p}),
    "RealName" -> text(minLength = 3, maxLength = 32),
    "Country" -> text(minLength = 2, maxLength = 32),
    "PhoneNumber" -> text.verifying(maxLength(18), minLength(9), pattern( """[0-9.+]+""".r, error = Messages("register.phoneError"))),
    "Role" -> ignored(NormalUser.value)
  )(Account.apply)(Account.unapply))

  /** *
    * Renders the HTML page for user/signin
    * @return 'views.html.User.signin'
    */
  def signin = Action {
    Ok(views.html.User.signin(userLoginForm))
  }

  def logout = Action.async { implicit request =>
    // do something...
    gotoLogoutSucceeded
  }

  /** *
    * Renders the HTML page for registering into the Application
    * @return 'views.html.User.register'
    */
  def register = Action {
    Ok(views.html.User.register(userRegisterForm))
  }

  /** *
    * Logins into the Application
    * @return TODO
    */
  def authenticate = Action.async { implicit request =>
    userLoginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.User.signin(formWithErrors))),
      user => {

        //TODO investigate why flatMap works and map doesn't. Possible alternatives?
        Account.authenticate(user) flatMap {
          bool => bool match {
            case true => gotoLoginSucceeded(user.Identifier)
            case _ => Future.successful(Redirect("/user/signin").flashing("error" -> "Fallo"))
          }
        }
      })
  }

  /** *
    * Submits a new registration into the Application
    * @return 'views.html.User.RegistrationSuccesful' | 'views.html.User.Register' with errors
    */
  //TODO: Its not working properly, doesn't redirect correctly onComplete
  def create = Action.async { implicit request =>
    userRegisterForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.User.register(formWithErrors))),
      user => {
        Account.registerNewAccount(user) onComplete {
          case Success(d) => Future.successful(Redirect("/user/signin").flashing("success" -> "Te has registrado correctamente"))
          case Failure(t) => Future.successful(Ok("Error 500"))
        }
        Future.successful(Ok("Error 500"))

        /*Account.registerNewAccount(user) map{
            result =>{
              Ok("Error 500")
            }
          }*/
      })
  }

  def myProfile = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "update Profile"
    Future.successful(Ok(views.html.User.profile(updateProfileForm, user)))
  }

  def updateProfile = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "update Profile"

    updateProfileForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.User.register(formWithErrors))),
      updatedUser => {
        val updatedAccount = Account(user.username,updatedUser.email,updatedUser.password,updatedUser.realName,updatedUser.country,updatedUser.phoneNumber,user.Role)

        Account.updateAccount(user.username, updatedAccount).map { res =>
            if (res.wasApplied())
              Redirect("/user/profile").flashing("success" -> "Te has registrado correctamente")
            else
              Ok("Error 500")
        }
      }
    )
  }

}//End-of-Controller
