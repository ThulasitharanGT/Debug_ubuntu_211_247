package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

import com.vsms.test.tmpAgg.pulsar.pulsarConsumer.consumerUtil.{getInputMap, getPulsarClient, printMsg, printMsgAndAck}
// works same as partition re-balance in kafka
object pulsarFailOver_1 {

  def main(args:Array[String]):Unit ={
    val inputMap= getInputMap(args)
    val pulsarClient= getPulsarClient(inputMap("serviceURL"))

    val pulsarConsumer= pulsarClient.newConsumer.consumerName(inputMap("consumerName"))
      .subscriptionName(inputMap("subscriptionName"))
      .subscriptionType(org.apache.pulsar.client.api.SubscriptionType.Failover)
      .topic(inputMap("topic")).subscribe

    while(pulsarConsumer.isConnected)
        pulsarConsumer.acknowledge(printMsgAndAck[Array[Byte]](pulsarConsumer.receive))


  }

}
