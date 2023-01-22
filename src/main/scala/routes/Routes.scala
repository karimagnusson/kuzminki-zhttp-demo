package routes

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import zio._
import zio.stream.ZStream
import zhttp.http._
import models.Query
import kuzminki.api.Jsonb

import java.io.{PrintWriter, CharArrayWriter}


object Routes {

  private val routes = SelectRoute.routes ++
                       OperationRoute.routes ++
                       CacheRoute.routes ++
                       StreamRoute.routes ++
                       JsonbRoute.routes ++
                       ArrayRoute.routes ++
                       DateRoute.routes

  val app = (routes).catchAll { ex =>
    Http.apply(
      Response.json(
        """{"error": "%s"}""".format(ex.getMessage)
      )
    )
  }
}


trait Routes {

  implicit class QueryParams(req: Request) {
    lazy val q = req.url.queryParams.map(p => p._1 -> p._2(0))
  }

  def withParams[R](req: Request)(fn: Map[String, String] => RIO[R, Response]): RIO[R, Response] = {
    for {
      body    <- req.body.asString
      params  <- Query.fromBody(body)
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






