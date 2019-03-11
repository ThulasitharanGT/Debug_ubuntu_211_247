package com.vsms.bean

class DataFrameFormatter(val DF_format:String, val DF_header:Boolean,val DF_inferSchema:Boolean, val DF_delimiter:String,val DF_SaveMode:String) {


  val format=DF_format
  val header=DF_header
  val inferSchema=DF_inferSchema
  val delimiter=DF_delimiter
  val SaveMode=DF_SaveMode

}
object DataFrameFormatter {
  val format_csv="com.databricks.spark.csv"
  val format_avro="com.databricks.spark.avro"
  val format_NA="NA"
  val header_true=true
  val header_false=false
  val inferSchema_false=false
  val inferSchema_true=true
  val delimiter_OR="|"
  val delimiter_Comma=","
  val delimiter_Tilde="~"
  val delimiter_Exclamation="!"
  val Save_Mode_Overwrite="overwrite"
  val Save_Mode__Append="append"
  val SaveMode_NA="NA"
}