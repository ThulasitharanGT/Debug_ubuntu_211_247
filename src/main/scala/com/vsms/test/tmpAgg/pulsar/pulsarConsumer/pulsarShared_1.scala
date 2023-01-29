package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

import com.vsms.test.tmpAgg.pulsar.pulsarConsumer.consumerUtil.printMsg
// same subscription name and diff consumer names are not splitting the workload, it's processing to payload n times, where n is the number of consumers
import scala.util.control.Breaks._
object pulsarShared_1 {
def main(args:Array[String]):Unit={

  val inputMap= collection.mutable.Map[String,String]()

  for (arg <- args)
    arg.split("=",2)match {
      case value => inputMap.put(value.head,value.last)
    }

  val pulsarObj = org.apache.pulsar.client.api.PulsarClient.builder.serviceUrl("pulsar://localhost:6650").build

  val pulsarConsumer= pulsarObj.newConsumer
    .consumerName(inputMap("consumerName"))
    .subscriptionName(inputMap("subscriptionName"))
    .subscriptionType(org.apache.pulsar.client.api.SubscriptionType.Shared)
    .topic(inputMap("topic")).subscribe
/*
  val pulsarConsumer2= pulsarObj.newConsumer
    .consumerName(inputMap("consumerName-2"))
    .subscriptionName(inputMap("subscriptionName"))
    .subscriptionType(org.apache.pulsar.client.api.SubscriptionType.Shared)
    .topic(inputMap("topic")).subscribe

  breakable
  {
  while( true)
    try {
      printMsg[Array[Byte]](pulsarConsumer.receive,Some("one".asInstanceOf[Any]))
      printMsg[Array[Byte]](pulsarConsumer2.receive,Some("two".asInstanceOf[Any]))
    }
    catch{
      case e:Exception =>
        break
    }
  }
 */

  while(pulsarConsumer.isConnected)
    printMsg[Array[Byte]](pulsarConsumer.receive,Some("one_1".asInstanceOf[Any]))


}
}
