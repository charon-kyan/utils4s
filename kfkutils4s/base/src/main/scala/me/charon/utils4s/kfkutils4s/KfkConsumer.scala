package me.charon.utils4s.kfkutils4s

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import org.slf4s.{Logger, LoggerFactory, Logging}


/**
  * Created by a on 1/22/17.
  */

trait KfkConsumer extends Logging {
  val consumeLog = LoggerFactory.getLogger("consume")

  object LogFormat extends Enumeration {
    type WeekDay = Value
    val Short, Long = Value
  }

  import LogFormat._
  val logFormat = Short

  def logConsume(topic: String, pid: String, offset: Long, msg: String) = {
    logFormat match {
      case Short => consumeLog.info("consume %s:%s for %s".format(pid, offset, topic))
      case Long => consumeLog.info("consume %s:%s for %s: %s".format(pid, offset, topic, msg))
    }
  }

  val group: String
  val topic: String


  def start(): Unit

  def run(): Unit

  def shutdown(): Unit
}

trait KfkConsumerWithProcessor extends KfkConsumer {
  self: Processor =>

  def prepare(): Unit

  def cleanup(): Unit

  def start() = {
    initProcessor()
    prepare()
  }

  def shutdown() = {
    cleanup()
    closeProcessor()
  }
}

trait FinishMarker {
  def markFinish(): Unit

  def finished(): Boolean

  def waitingAllFinished(log: Logger) = {
    while (!finished()) {
      Thread.sleep(500)
      if (log != null) log.info("waiting finish...")
    }
  }
}

trait FinishBooleanMarker extends FinishMarker {
  val finish = new AtomicBoolean(false)

  def markFinish() = finish.set(true)

  def finished() = finish.get
}

trait FinishCountDownMarker extends FinishMarker {
  val countDown: Int

  lazy val count = new CountDownLatch(countDown)

  def markFinish() = count.countDown()

  def finished() = count.getCount == 0
}