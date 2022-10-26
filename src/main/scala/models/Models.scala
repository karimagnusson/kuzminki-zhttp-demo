package models

import java.sql.Timestamp
import kuzminki.api._


object world {

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
    val capital = column[Int]("capital")
    val code2 = column[String]("code2")
  }

  Model.register[Country]

  class Lang extends Model("countrylanguage") {
    val countryCode = column[String]("countrycode")
    val language = column[String]("language")
    val isOfficial = column[Boolean]("isofficial")
    val percentage = column[BigDecimal]("percentage")
  }

  Model.register[Lang]

  class Trip extends Model("trip") {
    val id = column[Int]("id")
    val cityId = column[Int]("city_id")
    val price = column[Int]("price")
  }

  Model.register[Trip]

  // coins

  class CoinPrice extends Model("coin_price") {
    val coin = column[String]("coin")
    val price = column[BigDecimal]("price")
    val created = column[Timestamp]("created")
  }

  Model.register[CoinPrice]

  // jsonb

  class CountryData extends Model("country_data") {
    val id = column[Int]("id")
    val code = column[String]("code")
    val langs = column[Seq[String]]("langs")
    val data = column[Jsonb]("data")
    val cities = column[Jsonb]("cities")
  }

  Model.register[CountryData]

  class Place extends Model("place") {
    val code = column[String]("code")
    val places = column[Seq[Jsonb]]("places")
  }

  Model.register[Place]
}









