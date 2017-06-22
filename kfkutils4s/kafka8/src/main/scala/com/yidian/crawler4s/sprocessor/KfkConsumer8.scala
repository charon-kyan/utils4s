package com.yidian.crawler4s.sprocessor

import java.util.Properties
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import kafka.consumer.{Consumer, ConsumerConfig, ConsumerTimeoutException, KafkaStream}
import kafka.message.MessageAndMetadata

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by a on 1/22/17.
  */
trait KfkConsumer8 extends KfkConsumerWithProcessor {
  self: Processor with FinishMarker =>
  val zk: String

  def props: Properties = {
    val props = new Properties()
    props.put("zookeeper.connect", zk)
    props.put("group.id", group)
    props
  }

  lazy val consumer = Consumer.create(new ConsumerConfig(props))

  val running = new AtomicBoolean(true)

  def streams(n: Int): Seq[KafkaStream[Array[Byte], Array[Byte]]] = {
    val topicCountMap = Map(topic -> n)
    val consumerMap = consumer.createMessageStreams(topicCountMap)
    consumerMap.get(topic).get
  }

  def consumeAndProcess(stream: KafkaStream[Array[Byte], Array[Byte]]): Unit = {
    val it = stream.iterator()
    while (running.get()) {
      try {
        if (it.hasNext()) {
          val mdAndMsg: MessageAndMetadata[Array[Byte], Array[Byte]] = it.next()
          val msg = new String(mdAndMsg.message())
          logConsume(mdAndMsg.topic, mdAndMsg.partition.toString, mdAndMsg.offset, msg)
          try {
            process(msg)
          }
          catch {
            case e: Exception => log.error("process exception: " + msg, e)
          }
        }
      }
      catch {
        case e: ConsumerTimeoutException => log.info("consume is timeout")
      }
    }
  }

  override def cleanup() = {
    log.info("consumer is shutting down!")
    running.set(false)

    waitingAllFinished(log)
    consumer.commitOffsets(true)
    consumer.shutdown()

    log.info("consumer is shutdown!")
  }
}

trait KfkConsumer8S extends KfkConsumer8 with FinishBooleanMarker {
  self: Processor =>

  override def prepare() = {
    log.info("consumer is start!")
  }

  override def run() = {
    try {
      log.info("starting thread")
      consumeAndProcess(streams(1).head)
    } catch {
      case e: Exception => log.error("consume exception", e)
    } finally {
      markFinish()
      log.info("end thread")
    }
  }
}


trait KfkConsumer8P extends KfkConsumer8 with FinishCountDownMarker {
  self: Processor =>

  val threads: Int
  override lazy val countDown = threads
  implicit lazy val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threads))

  override def prepare() = {
    log.info("consumer is start!")
  }

  override def run() = {

    streams(threads).zipWithIndex map { case (stream, i) =>
      Future {
        try {
          log.info("starting thread: " + i)
          consumeAndProcess(stream)
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
