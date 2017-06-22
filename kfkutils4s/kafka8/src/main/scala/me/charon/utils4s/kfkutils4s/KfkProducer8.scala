package me.charon.utils4s.kfkutils4s

import java.util.Properties

/**
  * Created by a on 6/15/17.
  */
trait KfkProducer8 extends KfkProducer {

  import kafka.javaapi.producer.Producer
  import kafka.producer.{KeyedMessage, ProducerConfig}

  lazy val props = {
    val props = new Properties()
    props.put("metadata.broker.list", brokers)
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("key.serializer.class", "kafka.serializer.StringEncoder")
    props
  }

  lazy val producer: Producer[String, String] = new Producer(new ProducerConfig(props))

  override def produce(key: String, json: String) = {
    producer.send(new KeyedMessage[String, String](topic, key, json))
  }

  override def close() = producer.close
}

