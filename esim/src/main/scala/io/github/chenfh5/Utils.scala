package io.github.chenfh5

object Utils {

  /**
    * check whether task is running
    */
  def isTaskAlreadyExist(hashcode: Int): Boolean = {
    import collection.JavaConverters._
    Thread.getAllStackTraces.keySet.asScala.map(_.getName).count(_.contains(hashcode.toString)) > 0
  }

}
