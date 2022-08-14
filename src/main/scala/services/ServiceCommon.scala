package service

import zhttp.http._
import zhttp.service.Server
import zio._
import zio.blocking.Blocking
import play.api.libs.json._
import models.worlddb._
import models.PlayJsonLoader
import kuzminki.api._
import kuzminki.fn._


trait ServiceCommon {

  implicit val stringify: JsValue => CharSequence = obj => Json.stringify(obj)

  implicit val loadJson: Seq[Tuple2[String, Any]] => JsValue = { data =>
    PlayJsonLoader.load(data)
  }

  implicit val loadJsonString: Seq[Tuple2[String, Any]] => CharSequence = { data =>
    PlayJsonLoader.load(data).toString
  }
  
  val notFound: CharSequence = Json.obj("message" -> "not found").toString

  val routes: HttpApp[Has[Kuzminki] with Blocking, Throwable]
}