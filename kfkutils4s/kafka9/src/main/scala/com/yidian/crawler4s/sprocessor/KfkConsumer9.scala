package com.yidian.crawler4s.sprocessor

import java.util
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.errors.WakeupException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by a on 6/12/17.
  */
trait KfkConsumer9 extends KfkConsumerWithProcessor {
  self: Processor with FinishMarker =>

  val brokers: String

  val running: AtomicBoolean = new AtomicBoolean(true)

  def props = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, group)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

  def consumeAndProcess(consumer: KafkaConsumer[String, String]): Unit = {
    log.info("consumer starts to run!")
    consumer.subscribe(Collections.singletonList(this.topic))
    while (running.get()) {
      val consumerRecords: ConsumerRecords[String, String] = consumer.poll(1000)
      val it: util.Iterator[ConsumerRecord[String, String]] = consumerRecords.iterator
      while (it.hasNext) {
        val record: ConsumerRecord[String, String] = it.next
        val msg = record.value
        logConsume(record.topic, record.partition.toString, record.offset, msg)
        try {
          process(msg)
        }
        catch {
          case e: Exception => log.error("process exception: " + msg, e)
        }
      }
      // consumeLog.debug("%s records are processed".format(consumerRecords.count))
    }
  }
}

trait KfkConsumer9S extends KfkConsumer9 with FinishBooleanMarker {
  self: Processor =>

  lazy val consumer = new KafkaConsumer[String, String](props)

  override def prepare(): Unit = {
    log.info("consumer is start!")
  }

  override def cleanup() = {
    log.info("consumer is shutting down!")
    running.set(true)
    consumer.wakeup()

    waitingAllFinished(log)

    consumer.commitSync()
    consumer.close()
    log.info("consumer is shutdown!")
  }

  override def run() = {
    try {
      log.info("consumer starts to run!")
      consumeAndProcess(consumer)
    } catch {
      case e: WakeupException => if (running.get()) log.error("WakeupException when consuming", e)
      case e: Exception => log.error("Exception when consuming", e)
    } finally {
      markFinish()
      log.info("consumer is closed!")
    }
  }
}

trait KfkConsumer9P extends KfkConsumer9 with FinishCountDownMarker {
  self: Processor =>

  val threads: Int
  override lazy val countDown = threads

  lazy val consumers = (1 to threads) map { _ =>
    new KafkaConsumer[String, String](props)
  }

  override def prepare(): Unit = {
    log.info("consumer is start!")
  }

  override def cleanup() = {
    log.info("consumer is shutting down!")
    running.set(false)

    consumers.foreach(_.wakeup())

    waitingAllFinished(log)

    consumers.foreach { c =>
      c.commitSync()
      c.close()
    }

    log.info("consumer is shutdown!")
  }

  override def run() = {
    consumers.zipWithIndex map { case (consumer, i) =>
      Future {
        try {
          log.info("starting thread: " + i)
          consumeAndProcess(consumer)
        } catch {
          case e: Exception => log.error("consume exception", e)
        } finally {
          markFinish()
          log.info("end thread: " + i)
        }
      }
    }
  }
}
