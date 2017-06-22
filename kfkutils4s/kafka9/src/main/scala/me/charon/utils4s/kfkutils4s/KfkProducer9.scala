package me.charon.utils4s.kfkutils4s

import java.util.Properties

/**
  * Created by a on 6/15/17.
  */
trait KfkProducer9 extends KfkProducer {

  import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

  lazy val props = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  lazy val producer: KafkaProducer[String, String] =
    new KafkaProducer[String, String](props)

  override def produce(key: String, json: String) = {
    producer.send(new ProducerRecord(topic, key, json))
  }

  override def close() = producer.close()
}
