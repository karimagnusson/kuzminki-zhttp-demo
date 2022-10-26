package routes

import zio._
import zio.stream.ZStream
import zhttp.http._
import models.world._
import kuzminki.api.Jsonb


object Routes {

  private val routes = SelectRoute.routes ++
                       OperationRoute.routes ++
                       CacheRoute.routes ++
                       StreamRoute.routes ++
                       JsonbRoute.routes ++
                       ArrayRoute.routes

  val app = (routes).catchAll { ex =>
    //println(ex.getClass.getName)
    Http.apply(
      Response.json(
        """{"error": "%s"}""".format(ex.getMessage)
      )
    )
  }
}


trait Routes {

  def stringToMap(body: String) = ZIO.attempt {
    body
      .split("&")
      .map(_.split("="))
      .map(p => (p(0), p(1)))
      .toMap
  }

  def withParams[R](req: Request)(fn: Map[String, String] => RIO[R, Response]): RIO[R, Response] = {
    for {
      body    <- req.body.asString
      params  <- stringToMap(body)
      rsp     <- fn(params)
    } yield rsp
  }
  
  val notFound = """{"message": "not found"}"""

  val jsonObj: Jsonb => Response = { obj =>
    Response.json(obj.value)
  }

  val jsonOpt: Option[Jsonb] => Response = {
    case Some(obj) => Response.json(obj.value)
    case None => Response.json(notFound)
  }

  val jsonList: List[Jsonb] => Response = { list =>
    Response.json("[%s]".format(list.map(_.value).mkString(",")))
  }

  val jsonOk: Unit => Response = _ => jsonObj(Jsonb("""{"ok": true}"""))
}






