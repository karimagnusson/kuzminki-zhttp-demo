package routes

import java.util.UUID
import zio._
import zio.stream.{ZStream, ZPipeline, ZSink}
import zio.http._
import zio.http.Response
import java.sql.Timestamp
import models._
import kuzminki.api._
import kuzminki.fn._

// Examples for streaming.

object StreamRoute extends Responses {

  val coinPrice = Model.get[CoinPrice]
  val tempCoinPrice = Model.get[TempCoinPrice]

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
      t.created
    ))
    .cache
  
  val routes = Http.collectHandler[Request] {
    
    // Stream data as a csv file

    case Method.GET -> !! / "stream" / "export" / coin =>
      Handler.fromStream(
        sql
          .select(coinPrice)
          .cols3(t => (
            t.coin,
            Fn.roundStr(t.price, 2),
            t.created
          ))
          .where(_.coin === coin.toUpperCase)
          .orderBy(_.created.asc)
          .stream(500)
          .map(makeLine)
          .intersperse("\n")
      ).updateHeaders(_ ++ headers)

    // Stream csv file to the database
    // file: /csv/eth-price.csv

    case req @ Method.POST -> !! / "stream" / "import" =>
      Handler.fromZIO(
        req
          .body
          .asStream
          .via(ZPipeline.utf8Decode)
          .via(ZPipeline.splitLines)
          .map(parseLine)
          .transduce(insertCoinPriceStm.collect(500)) // Collect data to a Chunk of 500 rows
          .run(insertCoinPriceStm.asChunkSink) // Insert 500 rows each time.
          .map(jsonOk)
      )

    // Stream csv file into a temporary table.
    // If there are no errors, move the data from the temp table to the target table.
    // Then delete the data from the temp table. 

    case req @ Method.POST -> !! / "stream" / "import" / "safe" =>
      
      val uid = UUID.randomUUID

      Handler.fromZIO((for {

        _ <- req // Stream to temp table.
          .body
          .asStream
          .via(ZPipeline.utf8Decode)
          .via(ZPipeline.splitLines)
          .map(parseLine)
          .map {            // Add UUID used by the temp table.
            case (coin, price, takenAt) =>
              (uid, coin, price, takenAt)
          }
          .run(sql
            .insert(tempCoinPrice)
            .cols4(t => (
              t.uid,
              t.coin,
              t.price,
              t.created
            ))
            .cache
            .asSink
          )

        _ <- sql  // Move data in one statement using INSERT from SELECT.
          .insert(coinPrice)
          .cols3(t => (
            t.coin,
            t.price,
            t.created
          ))
          .fromSelect(
            sql
              .select(tempCoinPrice)
              .cols3(t => (
                t.coin,
                t.price,
                t.created
              ))
              .where(_.uid === uid)
          )
          .run

      } yield Response.json(okTrue)).tapEither { _ =>
        sql  // Delete from temp table on success or failure.
          .delete(tempCoinPrice)
          .where(_.uid === uid)
          .run
      })
  }
}













