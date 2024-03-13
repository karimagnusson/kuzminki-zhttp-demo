package routes

import zio._
import zio.http._
import models._
import kuzminki.api._
import kuzminki.column.TypeCol

// Examples for array field.

object ArrayRoute extends Responses {

  val countryData = Model.get[CountryData]

  val routes = Http.collectZIO[Request] {

    // Select a row with an array field.
    
    case Method.GET -> !! / "array" / "langs" / code =>
      sql
        .select(countryData)
        .colsJson(t => Seq(
          t.code,
          t.langs
        ))
        .where(_.code === code.toUpperCase)
        .runHeadOpt
        .map(jsonOpt(_))

    // Add to the array, make sure "lang" occurs once and sort the array ASC.

    case req @ Method.PATCH -> !! / "array" / "add" / "lang"  => withParams(req) { m =>
      sql
        .update(countryData)
        .set(_.langs addAsc m("lang"))
        .where(_.code === m("code"))
        .returningJson(t => Seq(
          t.code,
          t.langs
        ))
        .runHeadOpt
        .map(jsonOpt(_))
    }

    // Remove all instances of "lang" from the array.

    case req @ Method.PATCH -> !! / "array" / "del" / "lang"  => withParams(req) { m =>
      sql
        .update(countryData)
        .set(_.langs -= m("lang"))
        .where(_.code === m("code"))
        .returningJson(t => Seq(
          t.code,
          t.langs
        ))
        .runHeadOpt
        .map(jsonOpt(_))
    }
  }
}





















