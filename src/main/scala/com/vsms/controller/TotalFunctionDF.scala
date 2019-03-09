package com.vsms.controller

import com.vsms.DAO.{DFCreator, IOReadWrite}
import com.vsms.bean.DataFrameFormatter
import com.vsms.constants.{PathConstants, QueryBuilderConstants}
import com.vsms.service.ProjectFunctionDfVersion
import com.vsms.util.{DateFetcher, SparkOpener}
import org.apache.spark.sql.DataFrame

object TotalFunctionDF extends SparkOpener {

    //Total session
    val spark = SparkSessionLoc("DFCreator")
    spark.sparkContext.setLogLevel("ERROR")

    def main(args: Array[String]): Unit =
    {
      //val now = Calendar.getInstance().getTime()
      //print(now)

     // val Query_df = spark.read.option("inferSchema", "true").format("com.databricks.spark.csv").option("header", "true").option("delimiter", "|").load(ProjectConstants.INPUT_BASE_PATH + "\\Query\\Query_File.txt")
      //Query_df.createOrReplaceTempView("Query_View")
      //Query_df.show(10, false)

       // map for Arguments
      var ArgMap=collection.mutable.Map[String,String]()

     // map for DF input folders
      var DFInfoMap=collection.mutable.Map[String,String]()

     // table names
     DFInfoMap.put("Vehicle","Vehicle")
     DFInfoMap.put("Branch","Branch")
     DFInfoMap.put("Staff","Staff")
     DFInfoMap.put("Customer","Customer")

     // schema , delimiter and other stuff
      var formatter = new DataFrameFormatter("com.databricks.spark.csv",true,true,"|")

     // map for DF calling function and passing map as argument

     var DFMap= DFCreator.DFCreator(DFInfoMap,formatter)

     // map for passing DF

     var DFMap_pass=collection.mutable.Map[String,DataFrame]()

     // map for passing seq of columns for join condition

     var DFMap_List=collection.mutable.Map[String,Seq[String]]()

     //Testing SCN 1 with date between condition
     // putting parameters in map before passing

      DFMap_pass.put("%df1%",DFMap("Vehicle_df"))
      ArgMap.put(QueryBuilderConstants.WHERE_CONDITION,"to_date(DATE_OF_REGISTRATION) between to_date(\'2018-11-01 00:00:00\') and to_date(\'"+DateFetcher.timeCurrent(1)+"\') " )
      ArgMap.put("*","*")

     //Calling function

     val scn_1_date_df_main=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_1_date_df_main.show(false)

     //writing output

      IOReadWrite.DfOverWrite(scn_1_date_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN1_date_")

     //Testing SCN 1 with where condition
     // putting parameters in map before passing
     DFMap_pass.put("%df1%",DFMap(" "))
      ArgMap.put(QueryBuilderConstants.WHERE_CONDITION,"DESIGNATION = \'MECHANIC\' ")
      ArgMap.put("*","*")

     //Calling function

      val scn_1_str_df_main=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_1_str_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_1_str_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN1_str_")

     //Testing SCN 1 with number between condition
     // putting parameters in map before passing

      DFMap_pass.put("%df1%",DFMap("Customer_df"))
      ArgMap.put(QueryBuilderConstants.WHERE_CONDITION,"TOTAL_AMOUNT between 500000 and 700000")
      ArgMap.put("*","*")

     //Calling function

     val scn_1_int_df_main=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_1_int_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_1_int_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN1_int_")

     //Testing SCN 1 with date in where condition
     // putting parameters in map before passing

      DFMap_pass.put("%df1%",DFMap("Vehicle_df"))
      ArgMap.put(QueryBuilderConstants.WHERE_CONDITION," to_date(DATE_OF_DELIVERY) = to_date(\'2018-08-10 00:00:00\') ")
      ArgMap.put("*","*")

     //Calling function

     val scn_1_date_Equals_df_main=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_1_date_Equals_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_1_date_Equals_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN1_date_Equals_")

     //Testing SCN 2 with join condition and multiple column join
     // putting parameters in map before passing

      DFMap_pass.put("%df1%",DFMap("Vehicle_df"))
      DFMap_pass.put("%df2%",DFMap("Customer_df"))
                   //ArgMap.put("%join_condition%","\"VEHICLE_ID\",\"BRANCH_ID\",\"DATE_OF_DELIVERY\" ")
     DFMap_List.put("%join_condition%",Seq("VEHICLE_ID","BRANCH_ID","DATE_OF_DELIVERY"))
      ArgMap.put(QueryBuilderConstants.WHERE_CONDITION,"1=1")
      ArgMap.put(QueryBuilderConstants.JOIN_TYPE,"right")
     ArgMap.put(QueryBuilderConstants.DROP_A_CONDITION," ")
     ArgMap.put(QueryBuilderConstants.DROP_B_CONDITION," ")
     // 'inner', 'outer', 'full', 'fullouter', 'leftouter', 'left', 'rightouter', 'right', 'leftsemi', 'leftanti', 'cross'
      ArgMap.put("*","*")
                //ArgMap.put("*","\"BRANCH_CUSTOMER\",\"MODEL\",\"COUSTOMER_ID_BRANCH_ID\",\"DATE_OF_REGISTRATION\",\"DATE_OF_INTAKE\",\"STATUS\",\"VARIANT\",\"VEHICLE_ID\",\"NAME\",\"ADDRESS\",\"BRANCH_ID\",\"DATE_OF_INQUIRY\",\"CUSTOMER_ID\",\"BOOKING_ID\",\"DATE_OF_BOOKING\",\"DATE_OF_DELIVERY\",\"AMOUNT_PAID\",\"TOTAL_AMOUNT\",\"PAYMENT_MODE\",\"PAYMENT_ID\",\"MOBILE_NUMBER\",\"MAIL_ID\"")
                //ArgMap.put("*","\"a.BRANCH_CUSTOMER\",\"a.MODEL\",\"a.COUSTOMER_ID_BRANCH_ID\",\"a.DATE_OF_REGISTRATION\",\"a.DATE_OF_INTAKE\",\"a.STATUS\",\"a.VARIANT,\"b.*\"")


     //Calling function

     val scn_2_df_main=ProjectFunctionDfVersion.Retrival_info_fun(2,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_2_df_main.show(false)
                  // scn_2_df_main.select("VEHICLE_ID","BRANCH_CUSTOMER","DATE_OF_DELIVERY","MODEL","COUSTOMER_ID_BRANCH_ID","DATE_OF_REGISTRATION ","DATE_OF_INTAKE","STATUS","VARIANT","NAME","ADDRESS","DATE_OF_INQUIRY ","CUSTOMER_ID","BOOKING_ID","DATE_OF_BOOKING","AMOUNT_PAID","TOTAL_AMOUNT","PAYMENT_MODE","PAYMENT_ID","MOBILE_NUMBER","MAIL_ID").show(false)
                 // scn_2_df_main.show(false)
                 //.select(	"DATE_OF_INQUIRY","VEHICLE_ID","DATE_OF_BOOKING","NAME","VEHICLE_ID","DATE_OF_DELIVERY","DATE_OF_INTAKE","MOBILE_NUMBER","TOTAL_AMOUNT","ADDRESS","CUSTOMER_ID","VARIANT","BRANCH_ID","MODEL","COUSTOMER_ID_BRANCH_ID","BRANCH_CUSTOMER","AMOUNT_PAID","BOOKING_ID","PAYMENT_MODE","DATE_OF_DELIVERY","STATUS","BRANCH_ID","MAIL_ID","PAYMENT_ID","DATE_OF_REGISTRATION")

     //writing output

     IOReadWrite.DfOverWrite(scn_2_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN2_")

     //Testing SCN 3 with join condition and multiple column join
     // putting parameters in map before passing

// already parameters are put in map for testing before scenario

     //Calling function

     val scn_3_df_main=ProjectFunctionDfVersion.Retrival_info_fun(3,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_3_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_3_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN3_")

     //Testing SCN 2 with join condition and multiple column join a logical where condition
     // putting parameters in map before passing

     ArgMap.put(QueryBuilderConstants.WHERE_CONDITION," (TOTAL_AMOUNT-AMOUNT_PAID)<= ((TOTAL_AMOUNT/100) * 90) ")

    // println(DFMap_pass.contains("%df1%") )

     //Calling function

     val scn_2_condition1_df_main=ProjectFunctionDfVersion.Retrival_info_fun(2,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_2_condition1_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_2_condition1_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN2_condition2_")

     //Testing SCN 1 with where condition
     // putting parameters in map before passing

     DFMap_pass.put("%df1%",DFMap("Customer_df"))
     ArgMap.put("*","*")
     ArgMap.put(QueryBuilderConstants.WHERE_CONDITION," (TOTAL_AMOUNT-AMOUNT_PAID)<= ((TOTAL_AMOUNT/100) * 90) ")

     //Calling function

     val scn_2_condition_df_main=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_2_condition_df_main.show(false)
     //writing output

     IOReadWrite.DfOverWrite(scn_2_condition_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN2_condition_")

     //Testing Invalid SCN number

     //Calling function

     val scn_100_df_main=ProjectFunctionDfVersion.Retrival_info_fun(100,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     scn_100_df_main.show(false)

     //writing output

     IOReadWrite.DfOverWrite(scn_100_df_main,PathConstants.OUTPUT_BASE_PATH_DF+"SCN100_")


     //Testing Report with select condition
     // putting parameters in map before passing

     DFMap_pass.put("%df1%",DFMap("Branch_df"))
     ArgMap.put("*","*")
     ArgMap.put(QueryBuilderConstants.WHERE_CONDITION," 1=1 ")

     //Calling function

     val Profit_loss_report=ProjectFunctionDfVersion.Retrival_info_fun(1,DFMap_pass,ArgMap,DFMap_List)

     //displaying function output

     Profit_loss_report.show(false)
     //writing output

     IOReadWrite.DfOverWrite(Profit_loss_report,PathConstants.OUTPUT_BASE_PATH_DF+"Profit_loss_report_")
     /*
          DFMap_pass.put("%df1%",DFMap("Branch_df"))
          DFMap_List.put("*",Seq("BUDGET_REMAINING + TURN_OVER)/BUDGET_ALLOCATION ", "VEHICLES_SOLD", "MONTH_YEAR" ,"BRANCH_ID" ))
          ArgMap.put(QueryBuilderConstants.WHERE_CONDITION," 1=1 ")

          //Calling function

          val Sales_service_report=ProjectFunctionDfVersion.Retrival_info_fun(4,DFMap_pass,ArgMap,DFMap_List)
      */
     //val Sales_service_report=DFMap("Branch_df").select( expr("(BUDGET_REMAINING + TURN_OVER)/BUDGET_ALLOCATION"),"VEHICLES_SOLD", "MONTH_YEAR" ,"BRANCH_ID" )
     // val Sales_service_report=DFMap("Branch_df").selectExpr("BUDGET_REMAINING+TURN_OVER/BUDGET_ALLOCATION","VEHICLES_SOLD", "MONTH_YEAR" ,"BRANCH_ID" )
     //displaying function output

     // Sales_service_report.show(false)
     //writing output

     //IOReadWrite.DfOverWrite(Sales_service_report,PathConstants.OUTPUT_BASE_PATH_DF+"Sales_service_report_")

    }

 spark.close()


}
