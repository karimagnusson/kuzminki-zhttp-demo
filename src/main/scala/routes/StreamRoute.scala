package routes

import zio._
import zio.stream.{ZStream, ZPipeline, ZSink}
import zio.http._
import zio.http.Response
import java.sql.Timestamp
import models._
import kuzminki.api._
import kuzminki.fn._


object StreamRoute extends Routes {

  val coinPrice = Model.get[CoinPrice]

  val headers = Headers(
    Header.ContentType(MediaType.text.csv),
    Header.ContentDisposition.Attachment(Some("coins.csv"))
  )

  val makeLine: Tuple3[String, String, Timestamp] => String = {
    case (coin, price, takenAt) =>
      "%s,%s,%s".format(coin, price, takenAt.toString)
  }

  val parseLine: String => Tuple3[String, BigDecimal, Timestamp] = { line =>
    line.split(',') match {
      case Array(coin, price, takenAt) =>
        (coin, BigDecimal(price), Timestamp.valueOf(takenAt))
      case _ =>
        throw new Exception("invalid file")
    }
  }

  val insertCoinPriceStm = sql
    .insert(coinPrice)
    .cols3(t => (
      t.coin,
      t.price,
      t.stime
    ))
    .cache
  
  val routes = Http.collectHandler[Request] {
    
    case Method.GET -> !! / "stream" / "export" / coin =>
      Handler.fromStream(
        sql
          .select(coinPrice)
          .cols3(t => (
            t.coin,
            Fn.roundStr(t.price, 2),
            t.stime
          ))
          .where(_.coin === coin.toUpperCase)
          .orderBy(_.stime.asc)
          .streamBatch(500)
          .map(makeLine)
          .intersperse("\n")
      ).updateHeaders(_ ++ headers)

    // file: /csv/eth-price.csv

    case req @ Method.POST -> !! / "stream" / "import" =>
      Handler.fromZIO(
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













