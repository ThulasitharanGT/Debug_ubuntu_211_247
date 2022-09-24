package com.vsms.test.tmpAgg

import org.apache.pulsar.client.impl.schema.JSONSchema

object pulsarSchema {

  def main (args:Array[String]):Unit ={
    val pulsarClient =org.apache.pulsar.client.api.PulsarClient.builder
      .serviceUrl("pulsar://localhost:6650").build

    val pulsarProducer = pulsarClient
      .newProducer(JSONSchema.of(new tmpUserCool("",0).getClass.asInstanceOf[Class[tmpUserCool]]))
      .producerName("producer1")
      .topic("cool1")
      .create

    pulsarProducer.send(new tmpUserCool("coolTopic1",12))

    pulsarProducer.close
    pulsarClient.close
  }
}

