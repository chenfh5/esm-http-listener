package io.github.chenfh5

import org.slf4j.LoggerFactory
import org.testng.annotations.{AfterClass, BeforeClass, Test}

import scala.collection.mutable

class FuncTest {
  private val LOG = LoggerFactory.getLogger(getClass)
  val (host, port) = ("localhost", 9200)

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
    val controller = Controller(host, port, host, port)
    controller.process("your_indexname_src", "your_typename_src", "your_indexname_dest", 100)
  }

}
