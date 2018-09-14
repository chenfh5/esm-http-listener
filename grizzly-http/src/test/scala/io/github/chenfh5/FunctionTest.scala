package io.github.chenfh5

import io.github.chenfh5.OwnConfigReader.OwnConfig
import io.github.chenfh5.server.ShellServer
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.{AfterClass, BeforeClass, Test}

class FunctionTest {
  private val LOG = LoggerFactory.getLogger(getClass)

  @BeforeClass
  def setUp(): Unit = {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  @AfterClass
  def shut(): Unit = {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  @Test(enabled = true, priority = 1)
  def testShellServer(): Unit = {
    val shellServer = ShellServer(OwnConfigReader.OwnConfig.SERVER_HOST, OwnConfigReader.OwnConfig.HTTP_SERVER_PORT_1)
    println(s"this is the testShellServer NEED_AUTH=${OwnConfigReader.OwnConfig.NEED_AUTH}")
    (0 to 1).toList.par.foreach {
      case row if row % 2 == 0 =>
        println(s"thread id=${Thread.currentThread().getId}")
        shellServer.init()
        shellServer.start()
        Thread.sleep(5000) // server persistent manually
      case row if row % 2 == 1 =>
        Thread.sleep(1000) // wait for server bootstrap
        println(s"thread id=${Thread.currentThread().getId}")
        testShellClient()
        shellServer.stop()
    }
  }

  def testShellClient(): Unit = {
    val shellClient = ShellClient()
    val postBody =
      """
        {
          "source": "http://192.168.0.1:9200",
          "dest": "http://192.168.0.1:9201",
          "source_auth": "c5:c5",
          "dest_auth": "c5:c5",
          "src_indexes": "591_fuhaochen_2018070100",
          "shards": "9",
          "workers": "10",
          "bulk_size": "12",
          "count": "10000"
        }
      """
    val resp = shellClient.postEsmCmd(OwnConfig._AUTH64, postBody)
    println(resp.body)
    Assert.assertEquals(resp.code, 200)
  }

}
