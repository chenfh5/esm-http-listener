package io.github.chenfh5

import org.slf4j.LoggerFactory

import scala.collection.mutable

class CountQueueSize(queue: mutable.Queue[String]) extends Runnable {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    var producerHasData = false
    while (!producerHasData) {
      LOG.info("this is the CountQueueSize to wait producer generate data")
      Thread.sleep(1000)
      if (queue.nonEmpty) producerHasData = true
    }

    // report every 5 seconds
    while (queue.nonEmpty) {
      LOG.info(s"this is the CountQueueSize=${queue.size}")
      Thread.sleep(5000)
    }
  }

}
