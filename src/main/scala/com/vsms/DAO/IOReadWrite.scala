package com.vsms.DAO

import com.vsms.controller.TotalFunction.spark
import com.vsms.util.SparkOpener
import org.apache.spark.sql.{DataFrame, SaveMode}
import com.databricks.spark.avro._
import com.vsms.bean.DataFrameFormatter

object IOReadWrite extends SparkOpener {

  def DFAppend (DF : DataFrame, FolderPath :String) : Unit =
  {
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Append).parquet(FolderPath+"parq\\")
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Append).csv(FolderPath+"csv\\")
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Append).avro(FolderPath+"avro\\")
    DF.write.mode(SaveMode.Append).json(FolderPath+"json\\")
    println(FolderPath)

  }

  def DfOverWrite(DF : DataFrame, FolderPath :String) : Unit =
  {
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Overwrite).parquet(FolderPath+"parq\\")
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Overwrite).csv(FolderPath+"csv\\")
    DF.write.option("sep","|").option("header", "true").mode(SaveMode.Overwrite).avro(FolderPath+"avro\\")
    DF.write.mode(SaveMode.Overwrite).json(FolderPath+"json\\")
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
