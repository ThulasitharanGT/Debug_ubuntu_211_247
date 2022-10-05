package com.vsms.test.tmpAgg.pulsar

import org.apache.pulsar.functions.api.Context
import org.apache.pulsar.functions.api.Function

object pulsarFunctions extends Function[String,String]{
  override def process(input: String, context: Context) =s"${input}!"
}
