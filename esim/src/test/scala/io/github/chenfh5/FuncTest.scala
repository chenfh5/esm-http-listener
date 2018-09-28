package io.github.chenfh5

import org.slf4j.LoggerFactory
import org.testng.annotations.{AfterClass, BeforeClass, Test}

import scala.collection.mutable

class FuncTest {
  private val LOG = LoggerFactory.getLogger(getClass)

  @BeforeClass
  def setUp(): Unit = {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  @AfterClass
  def shut(): Unit = {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  // main body
  @Test(enabled = true)
  def testController(): Unit = {
    val queue = mutable.Queue[String]()
    val (srcHost, srcPort) = ("localhost", 9200)
    val (destHost, destPort) = ("localhost", 9201)
    val controller = new Controller(srcHost, srcPort, destHost, destPort, "Y2hlbmZoNQ==", "Y2hlbmZoNQ==")
    controller.process("your_indexname_src", "your_typename_src", "your_indexname_dest", 1000, 6)
  }

}
