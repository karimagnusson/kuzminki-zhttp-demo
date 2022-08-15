import zhttp.http._
import zhttp.service.Server
import zio._
import kuzminki.api._
import routes.Routes


object DemoServer extends zio.App {

  val dbConfig = DbConfig
    .forDb("world")
    //.withUser("")
    //.withPassword("")

  val kuzminkiLayer = Kuzminki.layer(dbConfig)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server
      .start(9000, Routes.app)
      .provideCustomLayer(kuzminkiLayer)
      .exitCode
}