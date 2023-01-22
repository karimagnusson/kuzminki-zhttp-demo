package routes

import zio._
import zhttp.http._
import models._
import kuzminki.api._
import kuzminki.fn._


object SelectRoute extends Routes {

  val city = Model.get[City]
  val country = Model.get[Country]
  val language = Model.get[Language]

  val routes = Http.collectZIO[Request] {

    case Method.GET -> !! / "select" / "country" / code =>
      sql
        .select(country)
        .colsJson(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map(jsonOpt(_))

    case Method.GET -> !! / "select" / "cities" / code =>
      sql
        .select(city, country)
        .colsJson(t => Seq(
          t.a.code,
          t.a.population,
          "city_name" -> t.a.name,
          "country_name" -> t.b.name,
          t.b.continent,
          t.b.region
        ))
        .joinOn(_.code, _.code)
        .where(_.b.code === code.toUpperCase)
        .orderBy(_.a.population.desc)
        .limit(5)
        .run
        .map(jsonList(_))

    case Method.GET -> !! / "select" / "language" / code  =>
      sql
        .select(country)
        .colsJson(t => Seq(
          t.code,
          t.name,
          sql
            .select(language)
            .colsJson(s => Seq(
              s.name,
              s.percentage
            ))
            .where(s => Seq(
              s.code <=> t.code,
              s.isOfficial === true
            ))
            .limit(1)
            .asColumn
            .first
            .as("language")
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map(jsonOpt(_))

    case Method.GET -> !! / "select" / "country-cities" / code  =>
      sql
        .select(country)
        .colsJson(t => Seq(
          t.code,
          t.name,
          Fn.json(Seq(
            t.continent,
            t.region,
            t.population
          )).as("info"),
          sql
            .select(city)
            .colsJson(s => Seq(
              s.name,
              s.population
            ))
            .where(_.code <=> t.code)
            .orderBy(_.population.desc)
            .limit(5)
            .asColumn
            .as("cities")
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map(jsonOpt(_))

    case req @ Method.GET -> !! / "select" / "optional" =>
      sql
        .select(country)
        .colsJson(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region,
          t.population
        ))
        .whereOpt(t => Seq(
          t.continent === req.q.get("cont"),
          t.region === req.q.get("region"),
          t.population > req.q.get("pop_gt").map(_.toInt),
          t.population < req.q.get("pop_lt").map(_.toInt)
        ))
        .orderBy(_.name.asc)
        .limit(10)
        .run
        .map(jsonList(_))

    case Method.GET -> !! / "select" / "and-or" / cont =>
      sql
        .select(country)
        .colsJson(t => Seq(
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
        .orderBy(t => Seq(
          t.population.desc,
          t.lifeExpectancy.desc
        ))
        .limit(10)
        .run
        .map(jsonList(_))

    case Method.GET -> !! / "select" / "population" / cont =>
      sql
        .select(country)
        .colsJson(t => Seq(
          "count" -> Count.all,
          "avg" -> Agg.avg(t.population),
          "max" -> Agg.max(t.population),
          "min" -> Agg.min(t.population)
        ))
        .where(_.continent === cont)
        .runHead
        .map(jsonObj(_))
        
  }
}












