package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._


object TripRoute extends Routes {

  val trip = Model.get[Trip]
  val city = Model.get[City]

  val routes = Http.collectZIO[Request] {
    
    case Method.GET -> !! / "trip" / "list" =>
      sql
        .select(trip, city)
        .colsNamed(t => Seq(
          t.a.id,
          t.a.price,
          t.b.name,
          t.b.countryCode
        ))
        .joinOn(_.cityId, _.id)
        .all
        .runAs[JsValue]
        .map(jsonList(_))

    case req @ Method.POST -> !! / "trip" / "add" => withBody(req) { obj =>

      val cityId = (obj \ "city_id").as[Int]
      val price = (obj \ "price").as[Int]

      sql
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
        .runHeadAs[JsValue]((cityId, price))
        .map(jsonObj(_))
    }

    case req @ Method.PATCH -> !! / "trip" / "update" => withBody(req) { obj =>

      val id = (obj \ "id").as[Int]
      val price = (obj \ "price").as[Int]

      sql
        .update(trip)
        .set(_.price ==> price)
        .where(_.id === id)
        .returningNamed(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }

    case req @ Method.DELETE -> !! / "trip" / "delete" => withBody(req) { obj =>

      val id = (obj \ "id").as[Int]

      sql
        .delete(trip)
        .where(_.id === id)
        .runNum
        .map(num => jsonObj(Json.obj("deleted" -> num)))
    }
  }
}