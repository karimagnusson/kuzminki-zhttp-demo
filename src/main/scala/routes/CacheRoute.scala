package routes

import zio._
import zio.http._
import models._
import kuzminki.api._


object CacheRoute extends Routes {

  val trip = Model.get[Trip]
  val city = Model.get[City]
  val country = Model.get[Country]

  val selectCountryStm = sql
    .select(country)
    .colsJson(t => Seq(
      t.code,
      t.name,
      t.continent,
      t.region
    ))
    .all
    .pickWhere1(_.code.use === Arg)
    .cache

  val selectJoinStm = sql
    .select(city, country)
    .colsJson(t => Seq(
      t.a.code,
      t.a.population,
      "city_name" -> t.a.name,
      "country_name" -> t.b.name,
      t.b.gnp,
      t.b.continent,
      t.b.region
    ))
    .joinOn(_.code, _.code)
    .where(t => Seq(
      t.b.continent === "Asia",
      t.b.gnp.isNotNull
    ))
    .orderBy(_.a.population.desc)
    .limit(5)
    .pickWhere2(t => (
      t.b.population.use >= Arg,
      t.b.gnp.use >= Arg
    ))
    .cache

  val insertTripStm = sql
    .insert(trip)
    .cols2(t => (
      t.cityId,
      t.price
    ))
    .returningJson(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val updateTripStm = sql
    .update(trip)
    .pickSet1(_.price.use ==> Arg)
    .pickWhere1(_.id.use === Arg)
    .returningJson(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val deleteTripStm = sql
    .delete(trip)
    .pickWhere1(_.id.use === Arg)
    .returningJson(t => Seq(
      t.id,
      t.cityId,
      t.price
    ))
    .cache

  val routes = Http.collectZIO[Request] {

    case Method.GET -> !! / "cache" / "select" / "country" / code =>
      selectCountryStm
        .runHeadOpt(code.toUpperCase)
        .map(jsonOpt(_))

    case Method.GET -> !! / "cache" / "join" / pop / gnp =>
      selectJoinStm
        .run(pop.toInt, BigDecimal(gnp))
        .map(jsonList(_))

    case req @ Method.POST -> !! / "cache" / "insert" / "trip" => withParams(req) { m =>
      insertTripStm
        .runHead((m("city_id").toInt, m("price").toInt))
        .map(jsonObj(_))
    }

    case req @ Method.PATCH -> !! / "cache" / "update" / "trip" => withParams(req) { m =>
      updateTripStm
        .runHeadOpt(m("price").toInt, m("id").toInt)
        .map(jsonOpt(_))
    }

    case req @ Method.DELETE -> !! / "cache" / "delete" / "trip" => withParams(req) { m =>
      deleteTripStm
        .runHeadOpt(m("id").toInt)
        .map(jsonOpt(_))
    }
  }
}