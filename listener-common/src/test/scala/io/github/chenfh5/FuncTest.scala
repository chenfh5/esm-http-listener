package io.github.chenfh5

import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.{AfterClass, BeforeClass, Test}

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

  @Test(enabled = true)
  def test(): Unit = {
    val now = OwnUtils.getTimeNow()
    Assert.assertTrue(now(4) == '-')
  }

  @Test(enabled = true)
  def testBase64(): Unit = {
    val rawMsg = "chenfh5"
    val enMsg = "Y2hlbmZoNQ=="

    Assert.assertEquals(enMsg, OwnUtils.encode(rawMsg))
    Assert.assertEquals(rawMsg, OwnUtils.decode(enMsg))
  }

}
