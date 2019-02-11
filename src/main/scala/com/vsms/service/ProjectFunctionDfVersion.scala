package com.vsms.service

import com.vsms.controller.TotalFunction.spark
import org.apache.spark.sql.DataFrame

object ProjectFunctionDfVersion
{

  /**
    * Created by DRIFTKINGDK on 1/5/2019.
    */

 // Function to perform calculation
  def Retrival_info_fun(scn_no: Int ,DFMap:  collection.mutable.Map[String,DataFrame], ArgMap : collection.mutable.Map[String,String], List_map: collection.mutable.Map[String,Seq[String]]) : DataFrame =

  {
//creating Error DataFrame
    val Error_df= spark.createDataFrame(Seq(("Empty_DF","Invalid SCN Number"))).toDF("Error","ERROR_MESSAGE")

    // SCN 1 single data frame with where and select conditions
    if (scn_no ==1)
    {
      var DF_final = DFMap("%df1%").filter(ArgMap("%where_condition%").toString).select(ArgMap("*").toString)
      return DF_final
    }
    // SCN 2 , 2 data frame with Join , Join type , aliases , multiple column in join ,where and select conditions

    else if (scn_no ==2)
    {
    //val DF_final= DFMap("df1").as("a").drop(ArgMap("%drop_coloumn_a%").toString).join(DFMap("df2").as("b").drop(ArgMap("%drop_coloumn_b%").toString),ArgMap("%join_condition%").toString).select(ArgMap("*").toString).filter(ArgMap("%where_condition%").toString)
    val DF_final= (DFMap("%df1%").drop(ArgMap("%drop_a_condition%"))).as("a").join((DFMap("%df2%").drop(ArgMap("%drop_b_condition%"))).as("b"),List_map("%join_condition%"),ArgMap("%join_type%").toString).select(ArgMap("*").toString).filter(ArgMap("%where_condition%").toString)
    return DF_final
    }

    // SCN 3 , 2 data frame with Join , aliases , multiple column in join ,where and select conditions

    else if (scn_no ==3)
    {
      val DF_final= (DFMap("%df1%").drop(ArgMap("%drop_a_condition%")).as("a")).join((DFMap("%df2%").drop(ArgMap("%drop_b_condition%"))).as("b"),List_map("%join_condition%")).select(ArgMap("*").toString).filter(ArgMap("%where_condition%").toString)
      return DF_final
    }
    Error_df
  }





}
