package routes

import zio._
import zio.http._
import models._
import kuzminki.api._

// INSERT, UPDATE, DELETE

object OperationRoute extends Responses {

  val trip = Model.get[Trip]
  val city = Model.get[City]

  val routes = Http.collectZIO[Request] {

    case req @ Method.POST -> !! / "insert" / "trip" => withParams(req) { m =>
      sql
        .insert(trip)
        .cols2(t => (
          t.cityId,
          t.price
        ))
        .values((
          m("city_id").toInt,
          m("price").toInt
        ))
        .returningJson(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHead
        .map(jsonObj(_))
    }

    case req @ Method.PATCH -> !! / "update" / "trip" => withParams(req) { m =>
      sql
        .update(trip)
        .set(_.price ==> m("price").toInt)
        .where(_.id === m("id").toInt)
        .returningJson(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadOpt
        .map(jsonOpt(_))
    }

    case req @ Method.DELETE -> !! / "delete" / "trip" => withParams(req) { m =>
      sql
        .delete(trip)
        .where(_.id === m("id").toInt)
        .returningJson(t => Seq(
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadOpt
        .map(jsonOpt(_))
    }
  }
}