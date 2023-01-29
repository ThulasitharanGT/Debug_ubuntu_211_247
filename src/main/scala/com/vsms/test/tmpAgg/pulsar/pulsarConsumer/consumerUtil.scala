package com.vsms.test.tmpAgg.pulsar.pulsarConsumer

object consumerUtil {
  def printMsg[T](msg: org.apache.pulsar.client.api.Message[T],info:Option[Any]=None): Unit ={
    println(info)
    info match {
      case Some(x) => println(x)
      case None => {}
    }
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

  def printMsgAndAck[T](msg: org.apache.pulsar.client.api.Message[T],info:Option[Any]=None) ={
    printMsg(msg)
    msg.getMessageId
  }

  def getPulsarClient(clientURL:String="https://localhost:6650")= org.apache.pulsar.client.api.PulsarClient.builder.serviceUrl(clientURL).build

  def getInputMap(args:Array[String])= args.foldLeft(collection.mutable.Map[String,String]().empty)((map,arg)=> arg.split("=",2) match {case argSplit => {map.put(argSplit(0),argSplit(1)) ; map} })

}
