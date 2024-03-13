package models

import com.typesafe.config.ConfigFactory
import kuzminki.api._


object Access {
  
  def getConfig = {

    val conf = ConfigFactory.load()

    DbConfig
      .forDb(conf.getString("db.name"))
      .withUser(conf.getString("db.user"))
      .withPassword(conf.getString("db.pwd"))
  }
}