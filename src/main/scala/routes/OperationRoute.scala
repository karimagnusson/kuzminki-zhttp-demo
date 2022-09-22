package routes

import zio._
import zhttp.http._
import play.api.libs.json._
import models.worlddb._
import kuzminki.api._


object OperationRoute extends Routes {

  val trip = Model.get[Trip]
  val city = Model.get[City]

  val routes = Http.collectZIO[Request] {

    case req @ Method.POST -> !! / "insert" / "trip" => withBody(req) { obj =>

      val cityId = (obj \ "city_id").as[Int]
      val price = (obj \ "price").as[Int]

      sql
        .insert(trip)
        .cols2(t => (
          t.cityId,
          t.price
        ))
        .values((cityId, price))
        .returningNamed(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadAs[JsValue]
        .map(jsonObj(_))
    }

    case req @ Method.PATCH -> !! / "update" / "trip" => withBody(req) { obj =>

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

    case req @ Method.DELETE -> !! / "delete" / "trip" => withBody(req) { obj =>

      val id = (obj \ "id").as[Int]

      sql
        .delete(trip)
        .where(_.id === id)
        .returningNamed(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadOptAs[JsValue]
        .map(jsonOpt(_))
    }
  }
}