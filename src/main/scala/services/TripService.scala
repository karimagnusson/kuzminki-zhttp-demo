package service

import zhttp.http._
import zhttp.service.Server
import zio._
import play.api.libs.json._
import models.worlddb._
import models.PlayJsonLoader
import kuzminki.api._
import kuzminki.fn._


object TripService extends ServiceCommon {

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
      .map(JsArray(_))
      .map(Response.json(_))

    case req @ Method.POST -> !! / "trip" / "add" =>

      val obj = Json.parse(req.body.toString)
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
        .map(Response.json(_))

    case req @ Method.PATCH -> !! / "trip" / "update" =>

      val obj = Json.parse(req.body.toString)
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
        .map {
          case Some(data) => Response.json(data)
          case None => Response.json(notFound)
        }

    case req @ Method.DELETE -> !! / "trip" / "delete" =>

      val obj = Json.parse(req.body.toString)
      val id = (obj \ "id").as[Int]

      sql
        .delete(trip)
        .where(_.id === id)
        .runNum
        .map(num => Response.json(Json.obj("deleted" -> num)))
  }
}