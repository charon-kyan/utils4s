package me.charon.utils4s.kfkutils4s

import java.util.Properties


/**
  * Created by a on 4/25/17.
  */
trait KfkProducer {
  val brokers: String
  val topic: String

  def produce(key: String, json: String): Unit

  def close(): Unit
}



