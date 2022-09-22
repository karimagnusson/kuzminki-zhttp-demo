import zhttp.http._
import zhttp.service.Server
import zio._
import kuzminki.api._
import routes.Routes


object DemoServer extends ZIOAppDefault {

  val dbConfig = DbConfig
    .forDb("world")
    //.withUser("")
    //.withPassword("")

  val dbLayer = Kuzminki.layer(dbConfig)

  def run = Server.start(9000, Routes.app).provide(dbLayer)
}