package routes

import zio._
import zio.http._
import models._
import kuzminki.api._
import kuzminki.fn._

// Examples of working with timestamp field.

object DateRoute extends Responses {

  val btcPrice = Model.get[BtcPrice]

  val routes = Http.collectZIO[Request] {

    case req @ Method.GET -> !! / "btc" / "hour" =>
      sql
        .select(btcPrice)
        .colsJson(t => Seq(
          t.high.round(2),
          t.low.round(2),
          t.open.round(2),
          t.close.round(2),
          t.created.format("DD Mon YYYY HH24:MI")
        ))
        .where(t => Seq(
          t.created.year === req.q("year").toInt, // pick year from timestamp
          t.created.doy === req.q("doy").toInt    // pick day of year from timestamp
        ))
        .orderBy(_.created.asc)
        .run
        .map(jsonList(_))

    case req @ Method.GET -> !! / "btc" / "quarter" / "avg" =>
      sql
        .select(btcPrice)
        .colsJson(t => Seq(
          "avg" -> Agg.avg(t.close).round(2),
          "max" -> Agg.max(t.close).round(2),
          "min" -> Agg.min(t.close).round(2)
        ))
        .where(t => Seq(
          t.created.year === req.q("year").toInt,
          t.created.quarter === req.q("quarter").toInt
        ))
        .runHead
        .map(jsonObj(_))

    case req @ Method.GET -> !! / "btc" / "break" =>
      sql
        .select(btcPrice)
        .colsJson(t => Seq(
          "price" -> t.high.round(2),
          "year" -> t.created.year,
          "quarter" -> t.created.quarter,
          "week" -> t.created.week,
          "date" -> t.created.format("DD Mon YYYY HH24:MI")
        ))
        .where(_.high >= BigDecimal(req.q("price")))
        .orderBy(_.high.asc)
        .limit(1)
        .runHeadOpt
        .map(jsonOpt(_))
  }
}












