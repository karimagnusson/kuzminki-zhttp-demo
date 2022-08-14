package models

import java.sql.Time
import java.sql.Date
import java.sql.Timestamp
import play.api.libs.json._


object PlayJsonLoader {

  val toJsValue: Any => JsValue = {
    case v: String      => JsString(v)
    case v: Boolean     => JsBoolean(v)
    case v: Short       => JsNumber(v)
    case v: Int         => JsNumber(v)
    case v: Long        => JsNumber(v)
    case v: Float       => JsNumber(v)
    case v: Double      => JsNumber(v)
    case v: BigDecimal  => JsNumber(v)
    case v: Time        => Json.toJson(v)
    case v: Date        => Json.toJson(v)
    case v: Timestamp   => Json.toJson(v)
    case v: Option[_]   => v.map(toJsValue).getOrElse(JsNull)
    case v: Seq[_]      => JsArray(v.map(toJsValue))
    case _              => throw new Exception("Cannot convert to JsValue")
  }

  def load(data: Seq[Tuple2[String, Any]]): JsValue = {
    JsObject(data.map(p => (p._1, toJsValue(p._2))))
  }
}