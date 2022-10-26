package routes

import zio._
import zio.stream.{ZStream, ZPipeline, ZSink}
import zhttp.http._
import java.sql.Timestamp
import models.world._
import kuzminki.api._
import kuzminki.fn._


object StreamRoute extends Routes {

  val coinPrice = Model.get[CoinPrice]

  val headers =
    Headers.contentType("text/csv") ++
    Headers.contentDisposition("attachment; filename=coins.csv")

  val makeLine: Tuple3[String, String, Timestamp] => String = {
    case (coin, price, created) =>
      "%s,%s,%s".format(coin, price, created.toString)
  }

  val parseLine: String => Tuple3[String, BigDecimal, Timestamp] = { line =>
    line.split(',') match {
      case Array(coin, price, created) =>
        (coin, BigDecimal(price), Timestamp.valueOf(created))
      case _ =>
        throw new Exception("invalid file")
    }
  }

  val insertCoinPriceStm = sql
    .insert(coinPrice)
    .cols3(t => (
      t.coin,
      t.price,
      t.created
    ))
    .cache
  
  val routes = Http.collectHttp[Request] {
    
    case Method.GET -> !! / "stream" / "export" / coin =>
      Http.fromStream(
        sql
          .select(coinPrice)
          .cols3(t => (
            t.coin,
            Fn.roundStr(t.price, 2),
            t.created
          ))
          .where(_.coin === coin.toUpperCase)
          .orderBy(_.created.asc)
          .streamBatch(500)
          .map(makeLine)
          .intersperse("\n")
      ).updateHeaders(_ ++ headers)

    // file: /csv/eth-price.csv

    case req @ Method.POST -> !! / "stream" / "import" =>
      Http.fromZIO(
        req
          .body
          .asStream
          .via(ZPipeline.utf8Decode)
          .via(ZPipeline.splitLines)
          .map(parseLine)
          .run(insertCoinPriceStm.asSink)
          .map(jsonOk)
      )
  }
}