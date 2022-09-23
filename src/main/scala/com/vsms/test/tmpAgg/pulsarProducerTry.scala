package com.vsms.test.tmpAgg
import org.apache.pulsar.common.schema.SchemaInfo



object pulsarProducerTry {
  def main(args:Array[String]):Unit ={

    val pulsarClient= org.apache.pulsar.client.api.PulsarClient.builder
      .serviceUrl("pulsar://localhost:6650")
      .build

    val pulsarProducer=pulsarClient.newProducer.producerName("prod1")
      .topic("coolTopic1")
      .create

    pulsarProducer.send("message".getBytes)

    pulsarProducer.close
    pulsarClient.close


  }

}
/*

class msgClass extends org.apache.pulsar.client.api.Schema[String] {
  override def encode(message: String): Array[Byte] = message.getBytes

  override def getSchemaInfo: SchemaInfo = ???
}
*/
