package models

//Still learning Scala. Had to use polymorphism
sealed trait Role{
  def value:String
}

case object NormalUser extends Role{
  def value="NormalUser"
}
case object TestUser extends Role {
  def value = "TestUser"
}




