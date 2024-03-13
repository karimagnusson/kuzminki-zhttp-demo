package routes

import zio._
import zio.http._
import zio.json._
import models._
import kuzminki.api._
import kuzminki.fn._

// Examples of using types and zio-json.

object TypeRoute extends Responses {

  val country = Model.get[Country]
  val trip = Model.get[Trip]

  // Decoders

  case class CountryType(code: String, name: String, population: Int)
  implicit val countryEncoder: JsonEncoder[CountryType] = DeriveJsonEncoder.gen[CountryType]

  case class TripType(id: Long, cityId: Int, price: Int)
  implicit val tripDecoder: JsonEncoder[TripType] = DeriveJsonEncoder.gen[TripType]

  // Encoders

  case class TripDataType(cityId: Int, price: Int)
  implicit val tripDataEncoder: JsonDecoder[TripDataType] = DeriveJsonDecoder.gen[TripDataType]

  case class TripPriceType(id: Long, price: Int)
  implicit val tripPriceEncoder: JsonDecoder[TripPriceType] = DeriveJsonDecoder.gen[TripPriceType]

  val routes = Http.collectZIO[Request] {

    case Method.GET -> !! / "type" / "select" / "country" / code =>
      sql
        .select(country)
        .cols3(t => (
          t.code,
          t.name,
          t.population
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOptType[CountryType]
        .map {
          case Some(rsp) => Response.json(rsp.toJson)
          case None      => Response.json(notFound)
        }

    case req @ Method.POST -> !! / "type" / "insert" / "trip" => withJsonBody(req) { json =>
      
      val data = json.fromJson[TripDataType] match {
        case Left(json)  => throw new Exception(s"Invalid data '$json'")
        case Right(data) => data
      }

      sql
        .insert(trip)
        .cols2(t => (
          t.cityId,
          t.price
        ))
        .valuesType(data)
        .returning3(t => (
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadType[TripType]
        .map(rsp => Response.json(rsp.toJson))
    }

    case req @ Method.PATCH -> !! / "type" / "update" / "trip" => withJsonBody(req) { json =>
      
      val data = json.fromJson[TripPriceType] match {
        case Left(json)  => throw new Exception(s"Invalid data '$json'")
        case Right(data) => data
      }

      sql
        .update(trip)
        .set(_.price ==> data.price)
        .where(_.id === data.id)
        .returning3(t => (
          t.id,
          t.cityId,
          t.price
        ))
        .runHeadOptType[TripType]
        .map {
          case Some(rsp) => Response.json(rsp.toJson)
          case None      => Response.json(notFound)
        }
    }
  }
}












