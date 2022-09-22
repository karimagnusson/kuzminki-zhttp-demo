package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._


object CacheRoute extends Routes {

  val trip = Model.get[Trip]
  val city = Model.get[City]
  val country = Model.get[Country]

  val selectCountryStm = sql
    .select(country)
    .colsNamed(t => Seq(
      t.code,
      t.name,
      t.continent,
      t.region
    ))
    .all
    .cacheWhere1(_.code.oprEq)

  val selectJoinStm = sql
    .select(city, country)
    .colsNamed(t => Seq(
      t.a.countryCode,
      t.a.population,
      "city_name" -> t.a.name,
      "country_name" -> t.b.name,
      t.b.gnp,
      t.b.continent,
      t.b.region
    ))
    .joinOn(_.countryCode, _.code)
    .where(t => Seq(
      t.b.continent === "Asia",
      t.b.gnp.isNotNull
    ))
    .orderBy(_.a.population.desc)
    .limit(5)
    .cacheWhere2(t => (
      t.b.population.oprGt,
      t.b.gnp.oprGte
    ))

  val insertTripStm = sql
    .insert(trip)
    .cols2(t => (
      t.cityId,
      t.price
    ))
    .returningNamed(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val updateTripStm = sql
    .update(trip)
    .pickSet1(_.price.modSet)
    .pickWhere1(_.id.oprEq)
    .returningNamed(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val deleteTripStm = sql
    .delete(trip)
    .pickWhere1(_.id.oprEq)
    .returningNamed(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val routes = Http.collectZIO[Request] {


    case Method.GET -> !! / "cache" / "select" / "country" / code =>
      selectCountryStm
        .runHeadOptAs[JsValue](code.toUpperCase)
        .map(jsonOpt(_))

    case Method.GET -> !! / "cache" / "join" / pop / gnp =>
      selectJoinStm
        .runAs[JsValue](pop.toInt, BigDecimal(gnp))
        .map(jsonList(_))

    case req @ Method.POST -> !! / "cache" / "insert" / "trip" => withBody(req) { obj =>

      val cityId = (obj \ "city_id").as[Int]
      val price = (obj \ "price").as[Int]

      insertTripStm
        .runHeadAs[JsValue]((cityId, price))
        .map(jsonObj(_))
    }

    case req @ Method.PATCH -> !! / "cache" / "update" / "trip" => withBody(req) { obj =>

      val id = (obj \ "id").as[Int]
      val price = (obj \ "price").as[Int]

      updateTripStm
        .runHeadOptAs[JsValue](price, id)
        .map(jsonOpt(_))
    }

    case req @ Method.DELETE -> !! / "cache" / "delete" / "trip" => withBody(req) { obj =>

      val id = (obj \ "id").as[Int]

      deleteTripStm
        .runHeadOptAs[JsValue](id)
        .map(jsonOpt(_))
    }
  }
}