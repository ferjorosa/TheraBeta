package Utility

import com.websudos.phantom.testing.CassandraFlatSpec
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
/**
 * Created by Fer on 04/03/2015.
 */
trait CustomSpec extends CassandraFlatSpec{
  val keySpace = "phantom"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  implicit val executor = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeAll(): Unit = {
    super.beforeAll()
    DatabaseService.init()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    DatabaseService.cleanup()
  }
}
