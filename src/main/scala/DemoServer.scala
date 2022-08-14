import zhttp.http._
import zhttp.service.Server
import zio._
import play.api.libs.json._
import models.worlddb._
import models.PlayJsonLoader
import kuzminki.api._
import kuzminki.fn._
import service._


object DemoServer extends zio.App {

  val dbConfig = DbConfig
    .forDb("world")
    .withUser("karimagnusson")
    .withPassword("localpass")

  val kuzminkiLayer = Kuzminki.layer(dbConfig)

  val app = (CountryService.routes ++ TripService.routes).catchAll { ex =>
    Http.apply(
      Response.json(Json.obj("error" -> ex.getMessage).toString)
    )
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server
      .start(9000, app)
      .provideCustomLayer(kuzminkiLayer)
      .exitCode
}