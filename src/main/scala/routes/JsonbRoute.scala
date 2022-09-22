package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._
import kuzminki.fn._
import kuzminki.column.TypeCol


object JsonbRoute extends Routes {

  val countryData = Model.get[CountryData]

  implicit val toJsonb: JsValue => Jsonb = obj => Jsonb(Json.stringify(obj))

  implicit class StringAsInt(col: TypeCol[String]) {
    def asInt = Cast.asInt(col)
  }

  val routes = Http.collectZIO[Request] {
    
    case Method.GET -> !! / "jsonb" / "country" / code =>
      sql
        .select(countryData)
        .colsNamed(t => Seq(
          t.id,
          t.code,
          t.langs,
          t.data
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))

    case Method.GET -> !! / "jsonb" / "capital" / name  =>
      sql
        .select(countryData)
        .colsNamed(t => Seq(
          t.id,
          t.code,
          t.langs,
          t.data - "cities"
        ))
        .where(_.data -> "capital" ->> "name" === name)
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))

    case Method.GET -> !! / "jsonb" / "city" / "population"  =>
      sql
        .select(countryData)
        .colsNamed(t => Seq(
          t.id,
          t.code,
          t.data ->> "name",
          t.data -> "cities" -> 0
        ))
        .where(t => (t.data -> "cities" -> 0 ->> "population").isNotNull)
        .orderBy(t => (t.data -> "cities" -> 0 ->> "population").asInt.desc)
        .limit(5)
        .runAs[JsValue]
        .map(jsonList)

    case Method.GET -> !! / "jsonb" / "capital-avg" / cont  =>
      sql
        .select(countryData)
        .colsNamed(t => Seq(
          Agg.avg((t.data #>> Seq("capital", "population")).asInt)
        ))
        .where(t => Seq(
          (t.data #>> Seq("capital", "population")).isNotNull,
          t.data ->> "continent" === cont
        ))
        .runHeadAs[JsValue]
        .map(jsonObj)

    case req @ Method.PATCH -> !! / "jsonb" / "add" / "phone"  => withBody(req) { obj =>
      
      val code = (obj \ "code").as[String]
      val phone = (obj \ "phone").as[String]

      sql
        .update(countryData)
        .set(_.data += Json.obj("phone" -> phone))
        .where(_.code === code)
        .returning1(_.data - "cities")
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }

    case req @ Method.PATCH -> !! / "jsonb" / "del" / "phone"  => withBody(req) { obj =>
      
      val code = (obj \ "code").as[String]

      sql
        .update(countryData)
        .set(_.data -= "phone")
        .where(_.code === code)
        .returning1(_.data - "cities")
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }
  }
}





















