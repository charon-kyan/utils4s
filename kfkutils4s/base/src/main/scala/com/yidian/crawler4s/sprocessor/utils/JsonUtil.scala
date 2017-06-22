package com.yidian.crawler4s.sprocessor.utils

import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}

/**
  * Created by a on 9/13/16.
  */
object JsonUtil {

  implicit val formats: Formats = new DefaultFormats {
    override val strictOptionParsing: Boolean = true
  }

  def mapToJson(doc: Map[String, Any]): String = {
    Serialization.write(doc)
  }

  def jsonToMap(json: String): Map[String, Any] = {
    // Serialization.read(json)
    implicit val formats = org.json4s.DefaultFormats
    parse(json).extract[Map[String, Any]]
  }

  def objectToJson[T <: AnyRef](obj: T): String = {
    Serialization.write(obj)
  }
}
