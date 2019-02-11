package com.vsms.bean

class DataFrameFormatter(var format:String, var header:Boolean,var inferSchema:Boolean, var delimiter:String) {


  override def toString = s"DataFrameFormatter($format, $header,$inferSchema, $delimiter)"
}
