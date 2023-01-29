package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

import com.vsms.test.tmpAgg.pulsar.pulsarConsumer.consumerUtil.{printMsg, printMsgAndAck}

object pulsarShared_2 {
def main(args:Array[String]):Unit={

  val inputMap=collection.mutable.Map[String,String]()

  args.map(arg => arg.split("=",2) match {case argSplit=> inputMap.put(argSplit(0),argSplit(1))} )

  val pulsarClient= org.apache.pulsar.client.api.PulsarClient.builder.serviceUrl(inputMap("pulsarURL"))
    .build

  val pulsarConsumer= pulsarClient.newConsumer
    .consumerName(inputMap("consumerName"))
    .subscriptionName(inputMap("subscriptionName"))
    .subscriptionType(org.apache.pulsar.client.api.SubscriptionType.Shared)
    .topic(inputMap("topic")).subscribe

  while(pulsarConsumer.isConnected)
    pulsarConsumer.acknowledge(printMsgAndAck[Array[Byte]](pulsarConsumer.receive,Some("one_1".asInstanceOf[Any])))

}
}
