package com.vsms.test.tmpAgg.pulsar

class pulsarJavaFunSplit extends java.util.function.Function[String,Unit] {

  val fruitTopic="com.fruit"
  val vegetableTopic="com.vegetable"
  val errorTopic="com.errorTopic"

  val fruitsList="Apple,Orange,Pineapple,Watermelon,Mozambi".split(",")
  val vegetableList="Carrot,Radish,Beans,Potato,Beetroot".split(",")

  val dataTuple= fruitsList.map(x => (x,"fruit")) ++ vegetableList.map(x => (x,"vegetable"))



  override def apply(input: String) = input.split("~") match {
    case value if dataTuple.filter(_._1.equalsIgnoreCase(value.head)).size > 0 =>
      sendProperMessage(value(1),dataTuple.filter(_._1.equalsIgnoreCase(value.head)).head._2)
    case _ => sendProperMessage(input,"error")

  }

  def getPulsarClient()=pulsarUtils.pulsarUtil("pulsar://localhost:6650")

  def getProducer[T](pulsarClient: pulsarUtils.pulsarUtil,topic:String,prodName:String,schema:Option[org.apache.pulsar.client.api.Schema[T]])=pulsarClient.getPulsarProducer[T](prodName,topic,schema)

  def sendProperMessage(msg:String,controller:String)= {
      val pulsarClient =getPulsarClient()
      getPulsarClient.getPulsarClient
      controller match {
        case "fruit" =>
          val producer:org.apache.pulsar.client.api.Producer[String]=getProducer[String](pulsarClient,fruitTopic,"fruitProd",Some(org.apache.pulsar.client.api.Schema.STRING))
          sendMessage[String](msg, producer)
          producer.close
        case "vegetable" =>
          val producer:org.apache.pulsar.client.api.Producer[String]=getProducer[String](pulsarClient,vegetableTopic,"vegetableProd",Some(org.apache.pulsar.client.api.Schema.STRING))
          sendMessage[String](msg, producer)
          producer.close
        case _ =>
          val producer:org.apache.pulsar.client.api.Producer[String]=getProducer[String](pulsarClient,errorTopic,"errorTopic",Some(org.apache.pulsar.client.api.Schema.STRING))
          sendMessage[String](msg, producer)
          producer.close
      }

    pulsarClient.closePulsarClient()
  }


  def sendMessage[T](message:T,prod:org.apache.pulsar.client.api.Producer[T]) = prod.send(message)
}

