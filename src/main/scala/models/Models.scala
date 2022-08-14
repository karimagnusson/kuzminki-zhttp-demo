package models

import play.api.libs.json._
import kuzminki.api._


object worlddb {

  case class CountrySlim(
    code: String,
    name: String,
    continent: String,
    region: String
  )

  implicit val countrySlimWrites = Json.writes[CountrySlim]

  class City extends Model("city") {
    val id = column[Int]("id")
    val name = column[String]("name")
    val countryCode = column[String]("countrycode")
    val district = column[String]("district")
    val population = column[Int]("population")
  }

  Model.register[City]

  class Country extends Model("country") {
    val code = column[String]("code")
    val name = column[String]("name")
    val continent = column[String]("continent")
    val region = column[String]("region")
    val surfaceArea = column[Float]("surfacearea")
    val indepYear = column[Short]("indepyear")
    val population = column[Int]("population")
    val lifeExpectancy = column[Float]("lifeexpectancy")
    val gnp = column[BigDecimal]("gnp")
    val gnpOld = column[BigDecimal]("gnpold")
    val localName = column[String]("localname")
    val governmentForm = column[String]("governmentform")
    val headOfState = column[String]("headofstate")
    val capital = column[String]("capital")
    val code2 = column[String]("code2")

    val slim = read[CountrySlim](code, name, continent, region)
  }

  Model.register[Country]

  class Trip extends Model("trip") {
    val id = column[Int]("id")
    val cityId = column[Int]("city_id")
    val price = column[Int]("price")
  }

  Model.register[Trip]
}









