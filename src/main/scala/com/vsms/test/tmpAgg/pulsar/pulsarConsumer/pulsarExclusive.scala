package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

import com.vsms.test.tmpAgg.pulsar.pulsarConsumer.consumerUtil.printMsg

object pulsarExclusive {
  def main(args:Array[String]):Unit ={

    val inputMap= collection.mutable.Map[String,String]()

    for (arg <- args)
      arg.split("=",2)match {
        case value => inputMap.put(value.head,value.last)
      }

    val pulsarObj = org.apache.pulsar.client.api.PulsarClient.builder.serviceUrl("pulsar://localhost:6650").build

    val pulsarConsumer= pulsarObj.newConsumer
      .consumerName(inputMap("consumerName"))
      .subscriptionName(inputMap("subscriptionName"))
      .subscriptionType(org.apache.pulsar.client.api.SubscriptionType.Exclusive)
      .topic(inputMap("topic")).subscribe

    while( pulsarConsumer.isConnected )
      printMsg(pulsarConsumer.receive)
  }


}
