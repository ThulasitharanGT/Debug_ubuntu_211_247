package com.vsms.util

import java.text.SimpleDateFormat
import java.util.Date

object DateFetcher {

  // Current time in 2 formats

  def timeCurrent(formatNeeded : Int) : String=
  {
    var DateObj = new Date()

    if(formatNeeded ==1) {
      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      val FinalDate = dateFormatter.format(DateObj)
      return FinalDate.toString()
    }
    else if (formatNeeded == 2)
    {
      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
      val FinalDate = dateFormatter.format(DateObj)
      return FinalDate.toString()
    }

    "Invalid Selection"
  }

}
