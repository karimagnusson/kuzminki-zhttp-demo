package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._
import kuzminki.fn._


object SelectRoute extends Routes {

  val city = Model.get[City]
  val country = Model.get[Country]

  val routes = Http.collectZIO[Request] {

    case Method.GET -> !! / "select" / "country" / code =>
      sql
        .select(country)
        .colsNamed(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    
    case Method.GET -> !! / "select" / "country-type" / code =>
      sql
        .select(country)
        .colsType(_.slim)
        .where(_.code === code.toUpperCase)
        .runHeadOptAs(Json.toJson(_))
        .map(jsonOpt(_))

    case Method.GET -> !! / "select" / "cities" / code =>
      sql
        .select(city, country)
        .colsNamed(t => Seq(
          t.a.countryCode,
          t.a.population,
          "city_name" -> t.a.name,
          "country_name" -> t.b.name,
          t.b.continent,
          t.b.region
        ))
        .joinOn(_.countryCode, _.code)
        .where(_.b.code === code.toUpperCase)
        .orderBy(_.a.population.desc)
        .limit(5)
        .runAs[JsValue]
        .map(jsonList(_))

    case req @ Method.GET -> !! / "select" / "optional" =>
      
      val params = req.url.queryParams.map(p => p._1 -> p._2(0))
      
      sql
        .select(country)
        .colsNamed(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region,
          t.population
        ))
        .whereOpt(t => Seq(
          t.continent === params.get("cont"),
          t.region === params.get("region"),
          t.population > params.get("pop_gt").map(_.toInt),
          t.population < params.get("pop_lt").map(_.toInt)
        ))
        .orderBy(_.name.asc)
        .limit(10)
        .runAs[JsValue]
        .map(jsonList(_))

    case Method.GET -> !! / "select" / "and-or" / cont =>
      sql
        .select(country)
        .colsNamed(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region,
          t.population,
          t.surfaceArea,
          t.lifeExpectancy,
          t.gnp
        ))
        .where(t => Seq(
          t.continent === cont,
          Or(
            And(
              t.population > 20000000,
              t.surfaceArea > 500000
            ),
            And(
              t.lifeExpectancy > 65,
              t.gnp > 150000
            )
          )
        ))
        .orderBy(_.name.asc)
        .limit(10)
        .runAs[JsValue]
        .map(jsonList(_))

    case Method.GET -> !! / "select" / "population" / cont =>
      sql
        .select(country)
        .colsNamed(t => Seq(
          "count" -> Count.all,
          "avg" -> Agg.avg(t.population),
          "max" -> Agg.max(t.population),
          "min" -> Agg.min(t.population)
        ))
        .where(_.continent === cont)
        .runHeadAs[JsValue]
        .map(jsonObj(_))
        
  }
}












