package io.github.chenfh5

import java.util.concurrent.atomic.AtomicBoolean

import org.slf4j.LoggerFactory

import scala.collection.mutable

class CountQueueSize(queue: mutable.Queue[String], getScrollDone: AtomicBoolean) extends Runnable {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    // report every 5 seconds
    while (queue.nonEmpty || !getScrollDone.get()) {
      LOG.info(s"this is the CountQueueSize=${queue.size}")
      Thread.sleep(5000)
    }
  }

}
