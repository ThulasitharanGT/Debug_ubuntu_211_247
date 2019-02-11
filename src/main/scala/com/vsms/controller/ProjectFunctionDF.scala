package com.vsms.controller
import org.apache.spark.sql.functions._
import com.vsms.util.SparkOpener
import com.vsms.DAO.IOReadWrite
import com.vsms.bean.DataFrameFormatter
import com.vsms.constants.PathConstants

object ProjectFunctionDF extends SparkOpener{
  val spark = SparkSessionLoc("Session")
  spark.sparkContext.setLogLevel("ERROR")

  def main(args: Array[String]): Unit =
  {

    // passing file information for avro through class
    var formatter = new DataFrameFormatter("com.databricks.spark.avro",true,true,"|")


   var temp=IOReadWrite.DFReadCsvAvro(PathConstants.OUTPUT_BASE_PATH_DF+"SCN2_condition_avro",formatter)
   temp.createOrReplaceTempView("temp_avro")
   temp.show(false)
// avro stores date in epoch format
    var temp_2= "select date_format(from_unixtime(DATE_OF_INQUIRY /1000 ), 'yyyy-MM-dd HH:mm:ss') from temp_avro "
    println(temp_2)
    spark.sql(temp_2.toString).show(false)
    var temp_3=IOReadWrite.DFReadJson(PathConstants.OUTPUT_BASE_PATH_DF+"SCN2_condition_json")
    temp_3.show(false)
    temp_3.createOrReplaceTempView("temp_json")
    var t_path="D:\\study\\Project_1.0\\TempFiles\\f1drivers_2018.json"
    var temp_jons=IOReadWrite.DFReadJson(t_path.toString)
    temp_jons.orderBy(desc("points")).show(false)
    temp_jons.createOrReplaceTempView("temp_jons")
    spark.sql("select Team ,sum(points) from  temp_jons group by Team order by sum(points) desc ").show(false)
      //.orderBy("Points")
    //spark.sql("select  from_unixtime(unix_timestamp(DATE_OF_INQUIRY), \'yyyy-mm-dd hh:MM:ss\') from  temp_json").show(false)

    var t_path_t="D:\\study\\Project_1.0\\Output_DF\\SCN1_date_Equals_json\\"
    var t_path_df=IOReadWrite.DFReadJson(t_path_t.toString)
    t_path_df.show(false)


  }
  spark.close()
}
