package com.vsms.DAO

import com.vsms.bean.DataFrameFormatter
import com.vsms.constants.PathConstants
import com.vsms.util.SparkOpener

object ViewCreator extends SparkOpener {

  def ViewCreator(TInfoMap : collection.mutable.Map[String, String],formatter: DataFrameFormatter): Unit= {

    var Table_Keys = TInfoMap.keys
    for (i <- Table_Keys)
    {
      var df=IOReadWrite.DFReadCsvAvro(PathConstants.INPUT_BASE_PATH +"\\"+TInfoMap(i)+"\\",formatter)
      df.createOrReplaceTempView(TInfoMap(i)+"_view")
    }
  }
}
