package com.vsms.controller

import com.vsms.DAO.{IOReadWrite, ViewCreator}
import com.vsms.bean.DataFrameFormatter
import com.vsms.constants.PathConstants
import com.vsms.util.{DateFetcher, SparkOpener}
import com.vsms.service.ProjectFunction

/**
  * Created by DRIFTKINGDK on 12/23/2018.
  */
object TotalFunction extends SparkOpener
{
//Total session
val spark = SparkSessionLoc("ViewCreator")
  spark.sparkContext.setLogLevel("ERROR")


  def main(args: Array[String]): Unit =
  {
    //val now = Calendar.getInstance().getTime()
    //print(now)

    // creating views

    var formatter = new DataFrameFormatter("com.databricks.spark.csv",true,true,"|")


    // map for Arguments

    var ArgMap=collection.mutable.Map[String,String]()

    // map for DF input folders
    var ViewInfoMap=collection.mutable.Map[String,String]()

    // table names
    ViewInfoMap.put("V","Vehicle")
    ViewInfoMap.put("B","Branch")
    ViewInfoMap.put("S","Staff")
    ViewInfoMap.put("C","Customer")


    //calling view creating function
    // map for DF calling function and passing map as argument

    ViewCreator.ViewCreator(ViewInfoMap,formatter)

    //calling  function

    val Query_df = IOReadWrite.DFReadCsvAvro(PathConstants.INPUT_BASE_PATH + "\\Query\\",formatter)

    //creating view

    Query_df.createOrReplaceTempView("Query_View")

    //Query_df.show(10, false)

    //Testing SCN 1 with date where and between condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Vehicle_View")
    ArgMap.put("%where_condition%","DATE_OF_REGISTRATION between to_date(\'2018-11-01\') and to_date(\'"+DateFetcher.timeCurrent(2)+"\')")
    ArgMap.put("*","*")

    //Calling function

    val scn_1_date_between_df_main=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    scn_1_date_between_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_1_date_between_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN1_date_between_")

    //Testing SCN 1 with where string
    // putting parameters in map before passing

    ArgMap.put("%t1%","Staff_View")
    ArgMap.put("%where_condition%","DESIGNATION = \'MECHANIC\' ")
    ArgMap.put("*","*")

    //Calling function

    val scn_1_str_df_main=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    scn_1_str_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_1_str_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN1_str_")

    //Testing SCN 1 with where and between int
    // putting parameters in map before passing

    ArgMap.put("%t1%","Customer_View")
    ArgMap.put("%where_condition%","TOTAL_AMOUNT between 500000 and 700000")
    ArgMap.put("*","*")

    //Calling function

    val scn_1_int_df_main=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    scn_1_int_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_1_int_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN1_int_")

    //Testing SCN 1 with where condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Vehicle_View")
    ArgMap.put("%where_condition%"," to_date(DATE_OF_DELIVERY) = to_date(\'2018-08-10\') ")
    ArgMap.put("*","*")

    //Calling function

    val scn_1_date_df_main=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    scn_1_date_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_1_date_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN1_date_")

    //Testing SCN 2 with join condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Vehicle_View v")
    ArgMap.put("%t2%","Customer_View c")
    ArgMap.put("%join%","join")
    ArgMap.put("%join_condition%"," v.VEHICLE_ID=c.VEHICLE_ID")
    ArgMap.put("*","v.BRANCH_CUSTOMER,v.MODEL,v.COUSTOMER_ID_BRANCH_ID,v.DATE_OF_REGISTRATION,v.DATE_OF_INTAKE,v.STATUS,v.VARIANT,c.*")

    //Calling function

    val scn_3_df_main=ProjectFunction.Retrival_info_fun(2,Query_df,ArgMap)

    //displaying function output

    scn_3_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_3_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN2_")

    //Testing SCN 3 with join condition and  where  condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Vehicle_View v")
    ArgMap.put("%t2%","Customer_View c")
    ArgMap.put("%join%","Right join")
    ArgMap.put("%join_condition%"," v.VEHICLE_ID=c.VEHICLE_ID")
    ArgMap.put("*","v.BRANCH_CUSTOMER,v.MODEL,v.COUSTOMER_ID_BRANCH_ID,v.DATE_OF_REGISTRATION,v.DATE_OF_INTAKE,v.STATUS,v.VARIANT,c.*")
    ArgMap.put("%where_condition%"," (c.TOTAL_AMOUNT-c.AMOUNT_PAID)<= ((c.TOTAL_AMOUNT/100) * 90) ")

    //Calling function

    val scn_4_df_main=ProjectFunction.Retrival_info_fun(3,Query_df,ArgMap)

    //displaying function output

    scn_4_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_4_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN3_")

    //Testing SCN 1 with date where  condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Customer_View c")
    ArgMap.put("*","*")
    ArgMap.put("%where_condition%"," (c.TOTAL_AMOUNT-c.AMOUNT_PAID)<= ((c.TOTAL_AMOUNT/100) * 90) ")

    //Calling function

    val scn_2_condition_df_main=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    scn_2_condition_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_2_condition_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN1_condition_")

    //Calling function

   val scn_100_df_main=ProjectFunction.Retrival_info_fun(100,Query_df,ArgMap)

    //displaying function output

    scn_100_df_main.show(false)

    //writing output

    IOReadWrite.DfOverWrite(scn_100_df_main,PathConstants.OUTPUT_BASE_PATH+"SCN100_")

    //Report with select condition
    // putting parameters in map before passing

    ArgMap.put("%t1%","Branch_view b")
    ArgMap.put("*","(BUDGET_REMAINING+TURN_OVER)/BUDGET_ALLOCATION as PLS,VEHICLES_SOLD,MONTH_YEAR,BRANCH_ID")
    ArgMap.put("%where_condition%"," 1=1 ")

    //Calling function

    val Sales_service_report=ProjectFunction.Retrival_info_fun(1,Query_df,ArgMap)

    //displaying function output

    Sales_service_report.show(false)

    //writing output

    IOReadWrite.DfOverWrite(Sales_service_report,PathConstants.OUTPUT_BASE_PATH+"Sales_service_report_")

  }
  

  }
