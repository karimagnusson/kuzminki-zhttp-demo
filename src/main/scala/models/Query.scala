package models

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import zio._


object Query {

  def decode(value: String) =
    URLDecoder.decode(value, StandardCharsets.UTF_8.toString)

  def fromBody(body: String) = ZIO.attempt {
    body
      .split('&')
      .map(_.split('='))
      .map(_.map(decode))
      .map(p => (p(0), p(1)))
      .toMap
  }
}


