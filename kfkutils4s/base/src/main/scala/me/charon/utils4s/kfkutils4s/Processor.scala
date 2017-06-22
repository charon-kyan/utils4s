package me.charon.utils4s.kfkutils4s

import org.slf4s.LoggerFactory


/**
  * Created by a on 4/25/17.
  */
trait Processor {

  val processLog = LoggerFactory.getLogger("process")

  // val fields: Map[String]

  def initProcessor(): Unit

  def closeProcessor(): Unit

  def process(msg: String): Unit

}
