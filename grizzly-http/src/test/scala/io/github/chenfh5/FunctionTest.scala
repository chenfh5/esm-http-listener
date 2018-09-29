package io.github.chenfh5

import io.github.chenfh5.server.ShellServer
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.{AfterClass, BeforeClass, Test}
import scalaj.http.HttpResponse

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

  @Test(enabled = false, priority = 1)
  def testShellServer(): Unit = {
    val shellServer = ShellServer(OwnConfigReader.OwnConfig.SERVER_HOST, OwnConfigReader.OwnConfig.HTTP_SERVER_PORT_1)
    println(s"this is the testShellServer NEED_AUTH=${OwnConfigReader.OwnConfig.NEED_AUTH}")
    (0 to 1).toList.par.foreach {
      case row if row % 2 == 0 =>
        println(s"thread id=${Thread.currentThread().getId}")
        shellServer.init()
        shellServer.start()
        while (shellServer.server != null) {
          Thread.sleep(5000) // server persistent manually
        }
      case row if row % 2 == 1 =>
        Thread.sleep(1000) // wait for server bootstrap
        println(s"thread id=${Thread.currentThread().getId}")
        val resp = testShellClient()
        println(resp)
        Assert.assertEquals(resp.code, 200)
        Assert.assertEquals(resp.body, "esim success")
        shellServer.stop()
    }
  }

  def testShellClient(): HttpResponse[String] = {
    import io.github.chenfh5.OwnConfigReader.OwnConfig
    import scalaj.http.Http
    val postBody =
      """
        {
          "srcHost": "localhost",
          "srcPort": "9200",
          "destHost": "localhost",
          "destPort": "9201",
          "authUser": "Y2hlbmZoNQ==",
          "authPW": "Y2hlbmZoNQ==",
          "srcIndexName": "your_indexname_src",
          "srcTypeName": "your_typename_src",
          "destIndexName": "your_indexname_dest",
          "scrollSize": "1000",
          "concurrentRequests": "6"
        }
      """
    val resp = Http(url = "http://%s:%s/%s".format(OwnConfig.SERVER_HOST, OwnConfig.HTTP_SERVER_PORT_1, "shell/esm")).header("Authorization", "chenfh5").postData(postBody).method("POST").asString
    resp
  }

}
