package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

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

  def printMsg[T](msg: org.apache.pulsar.client.api.Message[T]): Unit ={
    println(s"msg getKey ${msg.getKey}")
    println(s"msg getData ${msg.getData.map(_.toChar).mkString}")
    println(s"msg getIndex ${ scala.util.Try{msg.getIndex.get} match {
      case scala.util.Success(s) =>
        s
      case scala.util.Failure(f) =>
        ""
    }}")
    println(s"msg getEventTime ${msg.getEventTime}")
    println(s"msg getMessageId ${msg.getMessageId}")
    println(s"msg getSequenceId ${msg.getSequenceId}")
  }
}
