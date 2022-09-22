package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._


object ArrayRoute extends Routes {

  val countryData = Model.get[CountryData]

  implicit val toJsonb: JsValue => Jsonb = obj => Jsonb(Json.stringify(obj))

  val routes = Http.collectZIO[Request] {
    
    case Method.GET -> !! / "array" / "langs" / code =>
      sql
        .select(countryData)
        .colsNamed(t => Seq(
          t.code,
          t.langs,
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))

    case req @ Method.PATCH -> !! / "array" / "add" / "lang"  => withBody(req) { obj =>
      val code = (obj \ "code").as[String]
      val lang = (obj \ "lang").as[String]

      sql
        .update(countryData)
        .set(_.langs += lang)
        .where(_.code === code)
        .returningNamed(t => Seq(
          t.code,
          t.langs,
        ))
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }

    case req @ Method.PATCH -> !! / "array" / "del" / "lang"  => withBody(req) { obj =>
      val code = (obj \ "code").as[String]
      val lang = (obj \ "lang").as[String]

      sql
        .update(countryData)
        .set(_.langs -= lang)
        .where(_.code === code)
        .returningNamed(t => Seq(
          t.code,
          t.langs,
        ))
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }
  }
}





















