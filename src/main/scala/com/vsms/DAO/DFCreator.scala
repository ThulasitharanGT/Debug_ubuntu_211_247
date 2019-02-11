package com.vsms.DAO

import com.vsms.constants.PathConstants
import org.apache.spark.sql.DataFrame
import com.vsms.bean.DataFrameFormatter

object DFCreator
{
  def DFCreator(TInfoMap : collection.mutable.Map[String, String],formatter :DataFrameFormatter): collection.mutable.Map[String, DataFrame]=
  {
    var dfMap= collection.mutable.Map[String,DataFrame]()

    var Table_Keys = TInfoMap.keys
    for (i <- Table_Keys)
      {
        dfMap.put(TInfoMap(i)+"_df",IOReadWrite.DFReadCsvAvro(PathConstants.INPUT_BASE_PATH +"\\"+TInfoMap(i)+"\\",formatter))
       // println(i)
      }

    dfMap
  }
}
