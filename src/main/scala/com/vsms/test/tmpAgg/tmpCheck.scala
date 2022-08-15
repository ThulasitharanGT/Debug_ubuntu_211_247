package com.vsms.test.tmpAgg

 object tmpCheck{
   def main(args:Array[String]):Unit ={


     def findSubPalindrome(inputStr:String)= (for (i <- 0 to inputStr.size -1)
       yield {(for(j <- i+1 to inputStr.size -1) yield
         (i,inputStr.substring(i,j))
         ).foldLeft((-1,Array.empty[String]))((tup,currentValue) =>
         (currentValue._1, tup._2 :+ currentValue._2 )) }).
       foldRight(collection.mutable.Map[Int,Array[String]]())(
       (incomingSeq,map) =>{
         map.put(incomingSeq._1,incomingSeq._2)
         map
       }
     ).filter(_._1 != -1).map(x => (x._1,x._2.filter(_.size >1)))
       .map(x => (x._1,x._2.filter(s => s.reverse == s)))

     findSubPalindrome("ertyytrtfgh")

   }

 }