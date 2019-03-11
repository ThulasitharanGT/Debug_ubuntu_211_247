package com.vsms.DAO

import com.vsms.controller.TotalFunction.spark
import com.vsms.util.SparkOpener
import org.apache.spark.sql.{DataFrame, SaveMode}
import com.databricks.spark.avro._
import com.vsms.bean.DataFrameFormatter

object IOReadWrite extends SparkOpener {

  def DfWrite(DF : DataFrame, FolderPath :String,formatter :DataFrameFormatter) : Unit =
  {
    DF.write.option("sep",formatter.delimiter).option("header", formatter.header).mode(formatter.SaveMode).parquet(FolderPath+"parq\\")
    DF.write.option("sep",formatter.delimiter).option("header", formatter.header).mode(formatter.SaveMode).csv(FolderPath+"csv\\")
    DF.write.option("sep",formatter.delimiter).option("header", formatter.header).mode(formatter.SaveMode).avro(FolderPath+"avro\\")
    DF.write.mode(formatter.SaveMode).json(FolderPath+"json\\")
    println(FolderPath)

  }

  def DFReadCsvAvro(path: String,formatter :DataFrameFormatter) : DataFrame=
  {
    var df=spark.read.option("inferSchema", formatter.inferSchema).format( formatter.format).option("header", formatter.header).option("delimiter",formatter.delimiter).load(path.toString)
    df
  }

  def DFReadParq(path: String) : DataFrame=
  {
    var df=spark.read.load(path.toString)
    df
  }
  def DFReadJson(path: String) : DataFrame=
  {
    var df=spark.read.json(path.toString)
    df
  }
}
