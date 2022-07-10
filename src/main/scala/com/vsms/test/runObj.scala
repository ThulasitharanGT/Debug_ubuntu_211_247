package com.vsms.test

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.functions.{asc, col, count, lit, map_values, row_number}
import org.apache.spark.sql.types.{DecimalType, IntegerType, LongType, StringType, StructField, StructType}

object runObj {

  // readStreamFormat=delta path="hdfs://localhost:8020/user/raptor/persist/marks/GoldToTriggerInput/" goldCAPath="hdfs://localhost:8020/user/raptor/persist/marks/CA_Gold/" goldSAPath="hdfs://localhost:8020/user/raptor/persist/marks/SA_Gold/" semIdExamIDAssessmentYear="hdfs://localhost:8020/user/raptor/persist/marks/assessmentYearInfo_scd2/" checkpointLocation="hdfs://localhost:8020/user/raptor/stream/checkpoint/SAGoldToDiamondCalc" examIdToExamType="hdfs://localhost:8020/user/raptor/persist/marks/semIDAndExamIDAndExamType_scd2/" diamondPath="hdfs://localhost:8020/user/raptor/persist/marks/diamondCalculatedAndPartitioned/" semIdAndExamIdAndSubCode=hdfs://localhost:8020/user/raptor/persist/marks/semIDAndExamIDAndSubCode_scd2/

  def main(args:Array[String]) :Unit ={

    val inputMap=collection.mutable.Map[String,String]()

    args.map(_.split("=",2) match {case value => inputMap.put(value(0),value(1))})

    val spark=org.apache.spark.sql.SparkSession.builder.master("local[*]").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    val df=spark.read.format("delta").load(inputMap("goldCAPath")).
      union(spark.read.format("delta").load(inputMap("goldSAPath")))
      .filter("(studentId='stu001' and examId in ('e001','ex001')) or (studentId='stu002')")


    // assessmentYear
    val semIdExamIdAndAssessmentYearDF = spark.read.format("delta").load(inputMap("semIdExamIDAssessmentYear")).where("endDate is null").drop("endDate","startDate")
    // examType
    val examIdToExamTypeDF = spark.read.format("delta").load(inputMap("examIdToExamType")).where("endDate is null").drop("endDate","startDate")
    // examType
    val semIdAndExamIdToSubCodeDF = spark.read.format("delta").load(inputMap("semIdAndExamIdAndSubCode")).where("endDate is null").drop("endDate","startDate")



    examIdToExamTypeDF.createOrReplaceTempView("exam_type_table")
    semIdExamIdAndAssessmentYearDF.createOrReplaceTempView("assessment_year_table")
    semIdAndExamIdToSubCodeDF.createOrReplaceTempView("sem_exam_subject")

    spark.table("exam_type_table").as("a").join(
      spark.table("assessment_year_table").as("b"),"examId,semId".split(",").toSeq).
      withColumn("number_of_assessments_per_exam_type",
        count("examId").over(org.apache.spark.sql.expressions.Window
          .partitionBy("semId,b.assessmentYear,a.examType".split(",").map(col):_*))
      ).select("examId,semId,b.assessmentYear,a.examType,number_of_assessments_per_exam_type".split(",").map(col):_*)
      .as("b").join(spark.table("sem_exam_subject").as("b"),"examId,semId".split(",").toSeq)
      .orderBy("examId,semId,subjectCode".split(",").map(asc):_*).createOrReplaceTempView("exam_id_sem_id_exam_type")

    val examIDSemIDAndTypeMappedDF = spark.table("exam_id_sem_id_exam_type")

    examIDSemIDAndTypeMappedDF.persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK)
  //  spark.table("exam_id_sem_id_exam_type").withColumn("finalRef",lit("finalRef")).show(false)

    // takes semID of incoming examID

    val semIdDF = examIDSemIDAndTypeMappedDF
      .as("semIds")
      .join(df.as("trigger"),
        col("trigger.examId") === col("semIds.examId"))
      .select(col("trigger.examId"), col("semIds.semId"),
        col("trigger.studentId"), col("semIds.subjectCode")
        , $"semIds.examType", col("semIds.number_of_assessments_per_exam_type")
        , col("semIds.assessmentYear") ).withColumn("rankCol",
      row_number.over(org.apache.spark.sql.expressions.Window
        .partitionBy("semId", "examId","assessmentYear", "studentId"
          , "subjectCode", "examType")
        .orderBy(col("semId"))
      ))
      .where($"rankCol" === lit(1))
      .drop("rankCol")
      .orderBy("semId,examId,examType,subjectCode,studentId".split(",").toSeq.map(col): _*)

  //  semIdDF.withColumn("semIdDF", lit("semIdDF")).show(false)

    // takes examIds of incoming examID's semId

    val examIDsOfSemIdDF = examIDSemIDAndTypeMappedDF.as("semTarget")
      .join(semIdDF.as("semSource"), Seq("semId"))
      .selectExpr("semTarget.examId", "semId", "semSource.studentId"
        , "semTarget.subjectCode"
        , "semTarget.examType", "semTarget.number_of_assessments_per_exam_type as number_of_assessments") // cross joins exam type
      .withColumn("rankCol",
        row_number.over(org.apache.spark.sql.expressions.Window
          .partitionBy("semId", "examId", "studentId"
            , "subjectCode", "examType")
          .orderBy(col("semId"))
        ))
      .where($"rankCol" === lit(1))
      .drop("rankCol")
      .orderBy("semId,examId,examType,subjectCode,studentId".split(",").toSeq.map(col): _*)

    /*
   val examIDsOfSemIdDFTmp=examIDsOfSemIdDF.union(examIDsOfSemIdDF.withColumn("studentId",lit("s002")))
   val examIDsOfSemIdDF=examIDsOfSemIdDFTmp
    */

  //  examIDsOfSemIdDF.withColumn("examIDsOfSemIdDF", lit("examIDsOfSemIdDF")).show(false)

    val caExamIDsOfSemIdDF = examIDsOfSemIdDF.filter(col("examType") === lit("CA"))
    val saExamIDsOfSemIdDF = examIDsOfSemIdDF.filter(s"examType ='SA'")

    // examIDsOfSemIdDF.where(col("examType")=== lit(cumulativeAssessment)).filter("examId in ('e001')")
    // add examID partition to SA and CA in future

    val saExamIdAndStudentIdInfo = saExamIDsOfSemIdDF
      .map(x => Row(x.getAs[String]("examId"),
        x.getAs[String]("studentId")))(RowEncoder(new StructType(Array(
        StructField("examId", StringType, true)
        , StructField("studentId", StringType, true)
      )
      ))).collect.toSeq.map(x => (x.getAs[String]("examId"),
      x.getAs[String]("studentId"))).distinct

    val caExamIdAndStudentIdInfo = caExamIDsOfSemIdDF.select(
      "examId,studentId".split(",").toSeq.map(col): _*
    ).distinct.collect.toSeq.map(x => (x.getAs[String]("examId"),
      x.getAs[String]("studentId")))

    val caGoldInfo = spark.read.format("delta").load(inputMap("goldCAPath"))

    val saGoldInfo = spark.read.format("delta").load(inputMap("goldSAPath"))
    /*
    caGoldInfo.dtypes.map(x => x._2 match {
     case value if value.toLowerCase.contains("string") => s"""x.getAs[String]("${x._1}")"""
         case value if value.toLowerCase.contains("int") => s"""x.getAs[Int]("${x._1}")"""
     case value if value.toLowerCase.contains("decimal") => s"""x.getAs[java.util.BigDecimal]("${x._1}")"""
    })*/

    val saRecordsForIncomingKeysDF = saGoldInfo
      .where(s"examId in ${
        saExamIdAndStudentIdInfo.map(_._1) match {case value if value.size >0 => value.mkString("('","','","')") case value if value.size ==0 => "('')"}
      } and studentId in ${
        saExamIdAndStudentIdInfo.map(_._2) match {case value if value.size >0 => value.mkString("('","','","')") case value if value.size ==0 => "('')"}
      }").withColumn("examType", lit("SA"))
    //  .as("readFromGold")
    /*      .join(saGoldInfo
        .where(s"examId in ${getWhereCondition(
          caExamIdAndStudentIdInfo.map(_._1).toArray)} and studentId in ${getWhereCondition(
          caExamIdAndStudentIdInfo.map(_._2).toArray)}").groupBy("studentId").agg(countDistinct("examId").as("examsAttended"))
        .as("readFromGoldInner") , col("readFromGoldInner.studentId") === col("readFromGold.studentId")
      ).select("readFromGold.*","examsAttended")*/

   // saRecordsForIncomingKeysDF.withColumn("saRecordsForIncomingKeysDF", lit("saRecordsForIncomingKeysDF")).show(false)

    val caRecordsForIncomingKeysDF = caGoldInfo
      .where(s"examId in ${
        caExamIdAndStudentIdInfo.map(_._1) match {case value if value.size >0 => value.mkString("('","','","')") case value if value.size ==0 => "('')"}
      } and studentId in ${
        caExamIdAndStudentIdInfo.map(_._2) match {case value if value.size >0 => value.mkString("('","','","')") case value if value.size ==0 => "('')"}
      }").withColumn("examType", lit("CA"))

   // caRecordsForIncomingKeysDF.withColumn("saRecordsForIncomingKeysDF", lit("saRecordsForIncomingKeysDF")).show(false)


    // process, second level fite, as first level is defined to handle scn like yours

    val incomingRecords=caRecordsForIncomingKeysDF.union(saRecordsForIncomingKeysDF).
      filter("(studentId='stu001' and examId in ('e001','ex001')) or (studentId='stu002')")

    val semAndExamIdDF=examIDsOfSemIdDF

    incomingRecords.as("incoming").
      join(semAndExamIdDF.as("reference"),"examId,subjectCode,studentId,examType".split(",").toSeq
        ,"right").groupByKey(x => (x.getAs[String]("semId")
      ,x.getAs[String]("studentId")
      ,x.getAs[String]("examType"))).
      flatMapGroups((key,iterator) => {

        println("key "+key)

        val maxMarksNew=key._3 match {case "CA" => 40 case "SA"=> 60 }
        val passPercentageNew=key._3 match {case "CA" => 45 case "SA"=> 50 }
        val list=iterator.toList

        println("maxMarksNew "+maxMarksNew)
        println("passPercentageNew "+passPercentageNew)
        println("list "+list)

        val newMaxMarks=new java.math.BigDecimal(maxMarksNew.toString,java.math.MathContext.DECIMAL128)
          .divide(
            new java.math.BigDecimal(list.head.getAs[Long]("number_of_assessments"))
            ,java.math.MathContext.DECIMAL128)

        val newMarksList=list.map(x =>
          Row(x.getAs[String]("semId"),
            x.getAs[String]("examId"),
            x.getAs[String]("subjectCode"),
            x.getAs[String]("studentId"),
            x.getAs[String]("examType"),
            x.getAs[String]("assessmentYear"),
            getBigDecimalFromRow(x,"marks"),
            x.getAs[String]("grade"),
            x.getAs[String]("result"),
            x.getAs[Int]("passMarkPercentage"),
            x.getAs[Int]("maxMarks"),
            x.getAs[Int]("passMarkCalculated"),
            x.getAs[String]("comment"),
            x.getAs[Long]("number_of_assessments")
            , newMaxMarks
            ,  getBigDecimalFromInt(x.getAs[Int]("maxMarks")) match {
              case value if value==null =>
                println("maxMarks null")
                getBigDecimalFromInt()
              case value if getBigDecimalFromInt().compareTo(value)==0=>
                println("maxMarks 0")
                getBigDecimalFromInt()
              case value =>
                println("maxMarks value")
                getBigDecimalFromRow(x,"marks").multiply(getBigDecimalFromInt(100)
                  .divide(value,java.math.MathContext.DECIMAL128)
                  ,java.math.MathContext.DECIMAL128)
            }  // percentageOfMarksScored
            , (getBigDecimalFromInt(x.getAs[Int]("maxMarks")),getBigDecimalFromRow(x,"marks")) match {
              case (maxMarks,marks) if maxMarks == null || marks==null =>
                println("newMarks null")
                getBigDecimalFromInt()
              case (maxMarks,marks) if maxMarks.compareTo(getBigDecimalFromInt()) == 0 || marks.compareTo(getBigDecimalFromInt()) == 0 =>
                println("newMarks 00")
                getBigDecimalFromInt()
              case (maxMarks,marks) =>
                println(s"newMarks value value ${maxMarks} ${marks}")
                newMaxMarks.divide(getBigDecimalFromInt(100),java.math.MathContext.DECIMAL128)
                  .multiply(marks.multiply(getBigDecimalFromInt(100)
                    .divide(maxMarks,java.math.MathContext.DECIMAL128)
                    ,java.math.MathContext.DECIMAL128)
                    ,java.math.MathContext.DECIMAL128)
            }// newMarks
            , newMaxMarks.divide(getBigDecimalFromInt(100),java.math.MathContext.DECIMAL128).
              multiply(getBigDecimalFromInt(passPercentageNew),java.math.MathContext.DECIMAL128) // newPassMark
            ,list.filter(_.getAs[String]("subjectCode") == x.getAs[String]("subjectCode")).
              map( r => (r.getAs[Long]("number_of_assessments"),getBigDecimalFromRow(r,"marks")) )  match
            {case value /*if value.filter(_._2==null).size >0 */ =>value.filter(_._2!=null).size }   // num_of_assessments_attended
          )
        )

        /*
         RowEncoder(new StructType(Array(StructField("semId",StringType,true)
        ,StructField("examId",StringType,true)
        ,StructField("subjectCode",StringType,true)
        ,StructField("studentId",StringType,true)
        ,StructField("examType",StringType,true)
        ,StructField("assessmentYear",StringType,true)
        ,StructField("marks",DecimalType(6,3),true)
        ,StructField("grade",StringType,true)
        ,StructField("result",StringType,true)
        ,StructField("passMarkPercentage",IntegerType,true)
        ,StructField("maxMarks",IntegerType,true)
        ,StructField("passMarkCalculated",IntegerType,true)
        ,StructField("comment",StringType,true)
        ,StructField("number_of_assessments",LongType,true)
        ,StructField("newMaxMarks",DecimalType(6,3),true)
        ,StructField("percentageOfMarksScored",DecimalType(6,3),true) //15
        ,StructField("newMarks",DecimalType(6,3),true)
        ,StructField("newPassMark",DecimalType(6,3),true)
      ,StructField("num_of_assessments_attended",IntegerType,true)))
      )
      */

        val newResult=newMarksList.map(x =>
          Row(
            x.getAs[String](0), // semId
            x.getAs[String](1), //examId
            x.getAs[String](2), //subjectCode
            x.getAs[String](3), //studentId
            x.getAs[String](4),  //examType
            x.getAs[String](5), //assessmentYear
            x.getAs[java.math.BigDecimal](16), // new marks
            x.getAs[java.math.BigDecimal](17), // new pass marks
            x.getAs[java.math.BigDecimal](14), // new max marks
            x.getAs[Long](13), // num_of_assessments
            x.getAs[Int](18) , // num_of_assessments_attended
            x.getAs[String](5) match { // assessment year is checked for null
              case null => s"${x.getAs[String](1)}~${x.getAs[String](2)}" // examId~subCode
              case _ => ""
            }, // examId Not attended
            newMarksList.map(_.getAs[java.math.BigDecimal](14)).map( _ match {case null => getBigDecimalFromInt() case value =>  value}).
              foldLeft(getBigDecimalFromInt())((totalMarks,currentMarks) => totalMarks.add(currentMarks,java.math.MathContext.DECIMAL128)) // max Marks per examType
            , newMarksList.filter(_.getAs[String](2) == x.getAs[String](2)).map(_.getAs[java.math.BigDecimal](16)).
              foldRight(getBigDecimalFromInt())((currentMarks,totalMarks) => totalMarks.add(currentMarks,java.math.MathContext.DECIMAL128)) // subCode level total
            , newMarksList.filter(_.getAs[String](2) == x.getAs[String](2)).map(_.getAs[java.math.BigDecimal](17)).
              foldRight(getBigDecimalFromInt())((currentMarks,totalMarks) => totalMarks.add(currentMarks,java.math.MathContext.DECIMAL128)) // sub code level pass marks total
            , newMarksList.map(_.getAs[java.math.BigDecimal](17)).
              foldRight(getBigDecimalFromInt())((currentMarks,totalMarks) => totalMarks.add(currentMarks,java.math.MathContext.DECIMAL128)) // exam level pass marks total
            , newMarksList.map(_.getAs[java.math.BigDecimal](16)).map( _ match {case null => getBigDecimalFromInt() case value =>  value}).
              foldLeft(getBigDecimalFromInt())((totalMarks,currentMarks) => totalMarks.add(currentMarks,java.math.MathContext.DECIMAL128)) // examTypeLevel total
          )
        )
        /* new StructType(Array(StructField("semId",StringType,true)
          ,StructField("examId",StringType,true)
          ,StructField("subjectCode",StringType,true)
          ,StructField("studentId",StringType,true)
          ,StructField("examType",StringType,true)
          ,StructField("assessmentYear",StringType,true)
          ,StructField("marks",DecimalType(6,3),true)
          ,StructField("passMark",DecimalType(6,3),true)
          ,StructField("maxMarks",DecimalType(6,3),true)
          ,StructField("number_of_assessments",LongType,true)
          ,StructField("number_of_assessments_attended",IntegerType,true)
          ,StructField("comment_attendance",StringType,true)
          ,StructField("totalPerExamType",DecimalType(6,3),true)
          ,StructField("totalPerSubCodeLevel",DecimalType(6,3),true)
          ,StructField("totalPassMarkPerSubCodeLevel",DecimalType(6,3),true)
          ,StructField("totalPassMarkPerExamType",DecimalType(6,3),true)
           ,StructField("totalMarksPerExamType",DecimalType(6,3),true)

          */
        val subjectCodeList=newResult.map(_.getAs[String](2)).distinct

        subjectCodeList.map( subCode =>
          newResult.filter(_.getAs[String](2) == subCode) match {
            case value =>
              Row(key._1 , //semId
                key._2 , // studentId
                key._3 , // examType
                subCode,
                value.head.getAs[java.math.BigDecimal](13), // sub level total
                value.head.getAs[java.math.BigDecimal](14), // sub level pass mark
                getBigDecimalFromInt(maxMarksNew) , // max marks
                value.map(_ match { case x => (x.getAs[String](5),x.getAs[String](11))}).filter(_._1 == null).map(_._2).mkString(","), // combined exam level sub level comment
                value.head.getAs[java.math.BigDecimal](13).compareTo(value.head.getAs[java.math.BigDecimal](14)) match {
                  case value if List(0,1).contains(value) => "pass"
                  case -1 => "fail"
                }, // sub level result
                value.map(_.getAs[String](11)).filter(_.trim.size>0).flatMap(_.split("~")) match {case value if value.size% 2 ==0 && value.size !=0 => s"${value.grouped(2).map(_.toArray).foldLeft("")((commentStr,currentInfo) => commentStr.trim.size >0 match {case true => s"${commentStr},${currentInfo(0)}" case false => s"Did not attend ${currentInfo(0)}"  })} for ${value.last}" case _ => ""} // attendanceComment
              )
          }
        )  match {
          case value =>
            println(s"value in ${value}")
            value :+ Row(key._1, //semId
              key._2, // studentId
              key._3, // examType
              "subTotal",
              newResult.head.getAs[java.math.BigDecimal](16), // exam type level total
              newResult.head.getAs[java.math.BigDecimal](15), // exam level passmark total
              newResult.head.getAs[java.math.BigDecimal](12), // total mark per exam Type
              value.filter(_.getAs[String](8) =="fail").map(_.getAs[String](3)).mkString(","), // failed subjects comment
              value.filter(_.getAs[String](8) =="fail").size match {case value if value >0 => "FAIL" case _ => "PASS"},
              newResult.map(_.getAs[String](11)).filter(_.trim.size >0).map(_.split("~")).groupBy(_(1)).map(x => s"Did not appear in ${x._2.head(0)} for ${x._1} ." ).mkString("Attendance report: \n","\n","")
            )
        }

      })(RowEncoder( new StructType(
        Array(
          StructField("semId",StringType,true),
          StructField("studentId",StringType,true),
          StructField("examType",StringType,true),
          StructField("subCode",StringType,true),
          StructField("marks",DecimalType(6,3),true),
          StructField("passMarks",DecimalType(6,3),true),
          StructField("maxMarks",DecimalType(6,3),true),
          StructField("comments",StringType,true),
          StructField("result",StringType,true),
          StructField("attendanceComments",StringType,true)
        ))))  // no idea new group by key is not recognizing column names from the row encoder
      /*.groupByKey(x => (x.getAs[String]("semId")
      ,x.getAs[String]("studentId"),
    x.getAs[String]("subCode"))).mapGroups((key,dataIterator)=>{

      val dataList=dataIterator.toList

      println(s"dataList ${dataList}")

      val caRow=dataList.filter(_.getAs[String]("examType")=="CA").head
      val saRow=dataList.filter(_.getAs[String]("examType")=="SA").head

      Row(key._1, //semId
        key._2,//studentId
        key._3, //subCode
        caRow.getAs[java.math.BigDecimal]("marks"), // ca marks
        saRow.getAs[java.math.BigDecimal]("marks"), // sa marks
        caRow.getAs[java.math.BigDecimal]("passMarks"), // ca pass marks
        caRow.getAs[java.math.BigDecimal]("passMarks"), // sa pass marks
        caRow.getAs[java.math.BigDecimal]("marks").add(saRow.getAs[java.math.BigDecimal]("marks"),java.math.MathContext.DECIMAL128) // total marks
     )
    })*/.groupByKey(x => (x.getAs[String](0) // semId
      ,x.getAs[String](1) ) // studentId
    ).flatMapGroups((key,dataIterator)=>{

      val dataList=dataIterator.toList

      val totalRecord=dataList.filter(_.getAs[String](3)=="subTotal")
      val allOtherRecord=dataList.filter(_.getAs[String](3)!="subTotal")

      val semPassMark=new java.math.BigDecimal(60)
      println(s"dataList ${dataList}")

      val caList=allOtherRecord.filter(_.getAs[String](2)=="CA")
      val saList=allOtherRecord.filter(_.getAs[String](2)=="SA")

      val subList=allOtherRecord.map(_.getAs[String](3)).distinct

      subList.foldLeft(List.empty[Row]) ((rowList,incomingSub)=>
        {
          val caRow=caList.filter(_.getAs[String](3)==incomingSub).head
          val saRow=saList.filter(_.getAs[String](3)==incomingSub).head

          rowList :+ Row(key._1, //semId
            key._2,//studentId
            incomingSub , //subCode
            caRow.getAs[java.math.BigDecimal](4), // ca marks
            saRow.getAs[java.math.BigDecimal](4), // sa marks
            caRow.getAs[java.math.BigDecimal](5), // ca pass marks
            saRow.getAs[java.math.BigDecimal](5), // sa pass marks
            caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128), // total marks
            Array(0, 1).contains(caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128)
              .compareTo(semPassMark)) match {
              case true =>
                saRow.getAs[java.math.BigDecimal](4).compareTo(saRow.getAs[java.math.BigDecimal](5)) match {
                  case compResult if List(0, 1).contains(compResult) =>
                    caRow.getAs[java.math.BigDecimal](4).compareTo(caRow.getAs[java.math.BigDecimal](5)) match {
                      case compResult if List(0, 1).contains(compResult) => "Passed ,Cleared SA and CA"
                      case _ => "Passed ,Cleared to clear SA. But failed to clear CA"
                    }
                  case _ => "Failed to clear SA"
                }
              case false => "Failed to achieve pass mark in sem total"
            },
            (caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128)
              .compareTo(semPassMark), saRow.getAs[java.math.BigDecimal](4).compareTo(saRow.getAs[java.math.BigDecimal](5))) match {
              case value if getSuccessCheck(value._1) && getSuccessCheck(value._2) => "PASS"
              case _ => "FAIL"
            }
            ,s"${(Seq(caRow.getAs[String](7)):+saRow.getAs[String](7) ).filter(_.trim.size>0).mkString("~") }" // comments, concatenated By ~
            ,saRow.getAs[String](9),
            caRow.getAs[String](9)
          )
        }) match {
            case value =>
              val caRow=totalRecord.filter(_.getAs[String](2)=="CA").head
              val saRow=totalRecord.filter(_ match { case value => value.getAs[String](2)=="SA" }).head

              value :+ Row(
                value.head.getAs[String](0), // semId
                key._2, // studentId
                "subTotal", // subCode
                caRow.getAs[java.math.BigDecimal](4), //caMarks
                saRow.getAs[java.math.BigDecimal](4), //saMarks
                caRow.getAs[java.math.BigDecimal](5), //ca passMarks
                saRow.getAs[java.math.BigDecimal](5), //sa passMarks
                caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128), // totalMarks
                Array(0, 1).contains(caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128)
                  .compareTo(semPassMark.multiply(getBigDecimalFromInt(subList.size),java.math.MathContext.DECIMAL128))) match {
                  case true =>
                    saRow.getAs[java.math.BigDecimal](4).compareTo(saRow.getAs[java.math.BigDecimal](5)) match {
                      case compResult if List(0, 1).contains(compResult) =>
                        caRow.getAs[java.math.BigDecimal](4).compareTo(caRow.getAs[java.math.BigDecimal](5)) match {
                          case compResult if List(0, 1).contains(compResult) => "Passed ,Cleared SA and CA"
                          case _ => "Passed ,Cleared to clear SA. But failed to clear CA"
                        }
                      case _ => "Failed to clear SA"
                    }
                  case false => "Failed to achieve pass mark in sem total"
                },
                (caRow.getAs[java.math.BigDecimal](4).add(saRow.getAs[java.math.BigDecimal](4),java.math.MathContext.DECIMAL128)
                  .compareTo(semPassMark.multiply(getBigDecimalFromInt(subList.size),java.math.MathContext.DECIMAL128))
                  , saRow.getAs[java.math.BigDecimal](4).compareTo(saRow.getAs[java.math.BigDecimal](6))) match {
                  case value if getSuccessCheck(value._1) && getSuccessCheck(value._2) => "PASS"
                  case _ => "FAIL"
                },
              s"${(Seq(caRow.getAs[String](7)):+saRow.getAs[String](7) ).filter(_.trim.size>0).mkString("~")}"
                ,saRow.getAs[String](9).split("\n").map(_.trim).filter(_.size >0) match {case value if value.size ==1 => "" case value => saRow.getAs[String](9)},
              caRow.getAs[String](9).split("\n").map(_.trim).filter(_.size >0) match {case value if value.size ==1 => "" case value => caRow.getAs[String](9)}
              )
          }
        }
    )(RowEncoder(new StructType(Array(StructField("semId",StringType,true)
    ,StructField("studentId",StringType,true)
      ,StructField("subCode",StringType,true)
      ,StructField("ca_marks",DecimalType(6,3),true)
      ,StructField("sa_marks",DecimalType(6,3),true)
      ,StructField("ca_passMarks",DecimalType(6,3),true)
      ,StructField("sa_passMarks",DecimalType(6,3),true)
      ,StructField("semTotal",DecimalType(6,3),true)
      ,StructField("comment",StringType,true)
      ,StructField("result",StringType,true)
      ,StructField("attendanceComment",StringType,true)
      ,StructField("ca_attendanceComment",StringType,true)
      ,StructField("sa_attendanceComment",StringType,true)
    )))).show(false)



  }

  def getBigDecimalFromInt(intValue:Int=0)= new java.math.BigDecimal(intValue)
  def getBigDecimalFromDouble(intValue:Double=0.0)= new java.math.BigDecimal(intValue)

  def getBigDecimalFromRow(row:org.apache.spark.sql.Row,columnName:String)= row.getAs[java.math.BigDecimal](columnName)
  val getSuccessCheck=(result:Int) => List(0,1).contains(result)

}
