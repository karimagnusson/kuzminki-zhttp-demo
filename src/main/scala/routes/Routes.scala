package routes

import zio._
import zio.blocking.Blocking
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import models.PlayJsonLoader


object Routes {
  val app = (CountryRoute.routes ++ TripRoute.routes).catchAll { ex =>
    Http.apply(
      Response.json(
        Json.obj("error" -> ex.getMessage).toString
      )
    )
  }
}


trait Routes {

  implicit val loadJson: Seq[Tuple2[String, Any]] => JsValue = { data =>
    PlayJsonLoader.load(data)
  }

  def withBody[R](req: Request)(fn: JsValue => RIO[R, Response]): RIO[R, Response] = {
    for {
      body <- req.bodyAsString
      obj  <- Task.effect(Json.parse(body))
      rsp  <- fn(obj)
    } yield rsp
  }
  
  val notFound = Json.stringify(Json.obj("message" -> "not found"))

  val jsonObj: JsValue => Response = { obj =>
    Response.json(Json.stringify(obj))
  }

  val jsonOpt: Option[JsValue] => Response = {
    case Some(obj) => Response.json(Json.stringify(obj))
    case None => Response.json(notFound)
  }

  val jsonList: List[JsValue] => Response = { list =>
    Response.json(Json.stringify(JsArray(list)))
  }
}