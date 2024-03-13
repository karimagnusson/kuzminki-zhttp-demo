package routes

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import zio._
import zio.stream.ZStream
import zio.http._
import kuzminki.api.Jsonb

import java.io.{PrintWriter, CharArrayWriter}


object Routes {

  private val routes = SelectRoute.routes ++
                       OperationRoute.routes ++
                       CacheRoute.routes ++
                       StreamRoute.routes ++
                       JsonbRoute.routes ++
                       ArrayRoute.routes ++
                       DateRoute.routes ++
                       TypeRoute.routes

  val app = (routes).mapError { ex =>
    Response.json(
      """{"error": "%s"}""".format(ex.getMessage)
    )
  }
}


trait Responses {

  implicit class QueryParamsMap(req: Request) {
    val q = req.url.queryParams.map.map(p => p._1 -> p._2(0))
  }

  val bodyMap: Form => Map[String, String] = { form =>
    form.formData.map(f => f.name -> f.valueAsString.get).toMap
  }

  def withParams[R](req: Request)(fn: Map[String, String] => RIO[R, Response]): RIO[R, Response] = {
    for {
      params  <- req.body.asURLEncodedForm.map(bodyMap)
      rsp     <- fn(params)
    } yield rsp
  }

  def withJsonBody[R](req: Request)(fn: String => RIO[R, Response]): RIO[R, Response] = {
    for {
      params  <- req.body.asString
      rsp     <- fn(params)
    } yield rsp
  }
  
  val notFound = """{"message": "not found"}"""
  val okTrue = """{"ok": true}"""

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

  val jsonOk: Unit => Response = _ => jsonObj(Jsonb(okTrue))
}






