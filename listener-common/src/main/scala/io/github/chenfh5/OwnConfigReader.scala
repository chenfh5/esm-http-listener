package io.github.chenfh5

import java.util.Properties

object OwnConfigReader {

  private val properties = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("/config/variable.properties"))
    properties
  }

  object OwnConfig {
    var SERVER_HOST = getAsStr("SERVER_HOST")

    var HTTP_SERVER_PORT_1 = getAsInt("HTTP_SERVER_PORT_1")
    var NEED_AUTH = getAsBoolean("NEED_AUTH")
    var _AUTH64 = getAsStr("_AUTH64")

    var TIMEOUT_MILLS = getAsInt("TIMEOUT_MILLS")

    override def toString: String = super.toString
  }

  private def getAsInt(str: String): Int = properties.getProperty(str).toInt

  private def getAsBoolean(str: String): Boolean = properties.getProperty(str).toBoolean

  private def getAsStr(str: String): String = properties.getProperty(str)

}
