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

object IdentificationController extends AuthConfigImpl with LoginLogout {

  /**
   * Form-mapper used in the login action(only needs identifier (username) and password)
   */
  val userLoginForm: Form[UserLogin] = Form(mapping(
    "Identifier" -> text(minLength = 3, maxLength = 20),
    "Password" -> text(minLength = 3, maxLength = 20)
  )(UserLogin.apply)(UserLogin.unapply))

  /**
   * Form-mapperused in the register action (needs all values)
   */
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

  /**
   * Form-mapper used in the update profile action
   */
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
  def signin = Action.async { implicit request =>
    Future{Ok(views.html.User.signin(userLoginForm))}
  }

  def logout = Action.async { implicit request =>
    // do something...
    gotoLogoutSucceeded
  }

  /** *
    * Renders the HTML page for registering into the Application
    * @return 'views.html.User.register'
    */
  def register = Action.async {implicit request =>
    Future{Ok(views.html.User.register(userRegisterForm))}
  }

  /** *
    * Logins into the Application
    * @return
    */
  def authenticate = Action.async { implicit request =>
    userLoginForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.User.signin(formWithErrors))),
      user => Account.authenticate(user) flatMap {res =>
        if(res == true)
          gotoLoginSucceeded(user.Identifier)
        else
          Future.successful(Redirect("/user/signin").flashing("failure" -> Messages("signin.failure")))
      })
  }

  /** *
    * Submits a new registration into the Application
    * @return 'views.html.User.RegistrationSuccesful' | 'views.html.User.Register' with errors
    */
  def create = Action.async { implicit request =>
    userRegisterForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.User.register(formWithErrors))),
      user =>
        Account.findAccountByUsername(user.username)flatMap {
          case Some(user)=> Future.successful(Redirect("/user/register").flashing("failure" -> Messages("register.userAlreadyExists")))
          case None=> Account.registerNewAccount(user) map{res=>
            if(res == true)
              Redirect("/user/signin").flashing("success" -> Messages("register.successful"))
            else
              Redirect("/error/500")
          }
        })
  }

  /**
   * Redirects to the profile page after authorization
   * @return
   */
  def myProfile = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "my Profile"
    Future(Ok(views.html.User.profile(updateProfileForm, user)))
  }

  /**
   * Updates the user's profile
   * @return
   */
  def updateProfile = AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val title = "update Profile"

    updateProfileForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.User.profile(formWithErrors,user))),
      updatedUser => {
        val updatedAccount = Account(user.username,updatedUser.email,updatedUser.password,updatedUser.realName,updatedUser.country,updatedUser.phoneNumber,user.Role)

        Account.updateAccount(user.username, updatedAccount).map { res =>
            if (res == true)
              Redirect("/user/profile").flashing("success" -> Messages("profile.successful"))
            else
              Redirect("/error/500")
        }
      }
    )
  }

}//End-of-Controller
