import zio._
import zio.http._
import kuzminki.api._
import routes.Routes
import models.Access


object DemoServer extends ZIOAppDefault {

  val dbConfig = Access.getConfig
  val dbLayer = Kuzminki.layer(dbConfig)
  val configLayer = Server.defaultWithPort(9000)

  def run = Server.serve(Routes.app).provide(configLayer ++ dbLayer)
}