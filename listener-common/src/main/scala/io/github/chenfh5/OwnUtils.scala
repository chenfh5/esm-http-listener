package io.github.chenfh5

import java.text.{DateFormat, SimpleDateFormat}


object OwnUtils {

  private val sdfHiveFull = new ThreadLocal[DateFormat]() {
    override protected def initialValue(): DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E") // e.g., 2018-08-20 17:04:33 星期一
  }

  def getTimeNow(): String = {
    sdfHiveFull.get().format(System.currentTimeMillis())
  }

  def printInputArgs[T](args: T*): Unit = {
    println(">>>> this is the inputArgs begin")
    args.foreach(row => println(row.toString))
    println(">>>> this is the inputArgs end")
  }

  private val currentDir: String = new java.io.File("").getAbsolutePath

  def getCurrentDir: String = currentDir

  def getDirSep: String = scala.reflect.io.File.separator

  def makeFile[T](param: T*): String = param.mkString(getDirSep)

}