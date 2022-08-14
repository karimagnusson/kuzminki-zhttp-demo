package service

import zhttp.http._
import zhttp.service.Server
import zio._
import play.api.libs.json._
import models.worlddb._
import models.PlayJsonLoader
import kuzminki.api._
import kuzminki.fn._


object CountryService extends ServiceCommon {

  val city = Model.get[City]
  val country = Model.get[Country]

  val routes = Http.collectZIO[Request] {
    
    case Method.GET -> !! / "country" / "result1" / code =>
      sql
        .select(country)
        .colsType(_.slim)
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map {
          case Some(res) => Response.json(Json.toJson(res))
          case None => Response.json(notFound)
        }

    case Method.GET -> !! / "country" / "result2" / code =>
      sql
        .select(country)
        .colsNamed(t => Seq(
          t.code,
          t.name,
          t.continent,
          t.region
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map {
          case Some(res) => Response.json(res)
          case None => Response.json(notFound)
        }

    case Method.GET -> !! / "country" / "result3" / code =>
      sql
        .select(country)
        .cols4(t => (
          t.code,
          t.name,
          t.continent,
          t.region
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map {
          case Some(res) => res match {
            case (code, name, continent, region) =>
              Response.json(Json.obj(
                "code" -> code,
                "name" -> name,
                "continent" -> continent,
                "region" -> region
              ))
          }
          case None => Response.json(notFound)
        }

    case Method.GET -> !! / "top" / "cities" / code =>
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
        .limit(10)
        .runAs[JsValue]
        .map(JsArray(_))
        .map(Response.json(_))

    case req @ Method.GET -> !! / "country" / "optional" =>
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
        .whereOpts(t => Seq(
          t.continent === params.get("continent"),
          t.region === params.get("region"),
          t.population > params.get("population_gt").map(_.toInt),
          t.population < params.get("population_lt").map(_.toInt)
        ))
        .orderBy(_.name.asc)
        .limit(10)
        .runAs[JsValue]
        .map(JsArray(_))
        .map(Response.json(_))

    case Method.GET -> !! / "country" / "andor" / continent =>
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
          t.continent === continent,
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
        .map(JsArray(_))
        .map(Response.json(_))

    case Method.GET -> !! / "continent" / "population" / continent =>
      sql
        .select(country)
        .colsNamed(t => Seq(
          "count" -> Count.all,
          "avg" -> Avg.int(t.population),
          "max" -> Max.int(t.population),
          "min" -> Min.int(t.population)
        ))
        .where(_.continent === continent)
        .runHeadAs[JsValue]
        .map(Response.json(_))
        
  }
}












