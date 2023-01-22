package routes

import zio._
import zhttp.http._
import models._
import kuzminki.api._
import kuzminki.fn._
import kuzminki.column.TypeCol


object JsonbRoute extends Routes {

  val countryData = Model.get[CountryData]

  val routes = Http.collectZIO[Request] {
    
    case Method.GET -> !! / "jsonb" / "country" / code =>
      sql
        .select(countryData)
        .colsJson(t => Seq(
          t.uid,
          t.code,
          t.langs,
          t.data
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map(jsonOpt(_))

    case Method.GET -> !! / "jsonb" / "capital" / name  =>
      sql
        .select(countryData)
        .colsJson(t => Seq(
          t.uid,
          t.code,
          t.langs,
          (t.data || t.cities).as("data")
        ))
        .where(_.data -> "capital" ->> "name" === name)
        .runHeadOpt
        .map(jsonOpt(_))

    case Method.GET -> !! / "jsonb" / "city" / "population"  =>
      sql
        .select(countryData)
        .colsJson(t => Seq(
          t.uid,
          t.code,
          (t.data ->> "name").as("name"),
          (t.cities -> "cities" -> 0).as("largest_city")
        ))
        .where(t => (t.cities -> "cities" -> 0 ->> "population").isNotNull)
        .orderBy(t => (t.cities -> "cities" -> 0 ->> "population").asInt.desc)
        .limit(5)
        .run
        .map(jsonList)

    case Method.GET -> !! / "jsonb" / "capital-avg" / cont  =>
      sql
        .select(countryData)
        .colsJson(t => Seq(
          Agg.avg((t.data #>> Seq("capital", "population")).asInt)
        ))
        .where(t => Seq(
          (t.data #>> Seq("capital", "population")).isNotNull,
          t.data ->> "continent" === cont
        ))
        .runHead
        .map(jsonObj)

    case req @ Method.PATCH -> !! / "jsonb" / "add" / "phone"  => withParams(req) { m =>
      sql
        .update(countryData)
        .set(_.data += Jsonb("""{"phone": "%s"}""".format(m("phone"))))
        .where(_.code === m("code"))
        .returning1(_.data)
        .runHeadOpt
        .map(jsonOpt(_))
    }

    case req @ Method.PATCH -> !! / "jsonb" / "del" / "phone"  => withParams(req) { m =>
      sql
        .update(countryData)
        .set(_.data -= "phone")
        .where(_.code === m("code"))
        .returning1(_.data)
        .runHeadOpt
        .map(jsonOpt(_))
    }
  }
}





















