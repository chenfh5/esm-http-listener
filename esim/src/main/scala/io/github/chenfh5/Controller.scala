package io.github.chenfh5

import java.util.concurrent.atomic.AtomicBoolean

import io.github.chenfh5.OwnConfigReader.OwnConfig
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory

import scala.collection.mutable

class Controller(srcHost: String, srcPort: Int, destHost: String, destPort: Int, authUser: String, authPW: String) extends LifeCycle {
  private val LOG = LoggerFactory.getLogger(getClass)
  var srcClient: RestHighLevelClient = _
  var destClient: RestHighLevelClient = _
  val queue: mutable.Queue[String] = scala.collection.mutable.Queue[String]()
  var getScrollDone = new AtomicBoolean(false)

  override def setup(): Unit = {
    import org.apache.http.HttpHost
    import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
    import org.apache.http.impl.client.BasicCredentialsProvider
    import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
    import org.elasticsearch.client.RestClient
    val credentialsProvider = new BasicCredentialsProvider()
    if (StringUtils.isNoneBlank(authUser)) OwnConfig._ESUSER = authUser
    if (StringUtils.isNoneBlank(authPW)) OwnConfig._ESPASSWORD = authPW
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(OwnUtils.decode(OwnConfig._ESUSER), OwnUtils.decode(OwnConfig._ESPASSWORD)))

    srcClient = new RestHighLevelClient(RestClient.builder(new HttpHost(srcHost, srcPort, "http")).setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) => httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)))
    destClient = new RestHighLevelClient(RestClient.builder(new HttpHost(destHost, destPort, "http")).setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) => httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)))
    LOG.info("this is the setup begin={}", OwnUtils.getTimeNow())
  }

  override def teardown(): Unit = {
    srcClient.getLowLevelClient.close()
    destClient.getLowLevelClient.close()
    LOG.info("this is the teardown end={}", OwnUtils.getTimeNow())
  }

  def process(srcIndexName: String, srcTypeName: String, destIndexName: String, scrollSize: Int = 10000, concurrentRequests: Int = 5): String = {
    require(scrollSize <= 10000, "Batch size <= 10000")
    require(concurrentRequests <= 20, "concurrentRequests <= 20")
    LOG.info(s"this is the Controller begin at ${OwnUtils.getTimeNow()}")
    val beginTime = System.nanoTime()
    setup()
    val copyIndex = CopyIndex()
    val res = copyIndex.copy(srcClient, destClient, srcIndexName, srcTypeName, destIndexName)
    if (!res) throw new RuntimeException("create copy index fail")

    // producer
    val getScroll = new GetScroll(srcClient, srcIndexName, scrollSize, queue, getScrollDone)
    // consumer
    val putBulk = new PutBulk(destClient, destIndexName, srcTypeName, concurrentRequests, queue, getScrollDone)
    putBulk.setup()
    // reporter
    val countQueueSize = new CountQueueSize(queue, getScrollDone)

    // start thread
    // @see https://stackoverflow.com/questions/20495414/thread-join-equivalent-in-executor
    val tp = Seq(new Thread(getScroll), new Thread(putBulk), new Thread(countQueueSize))
    tp.foreach(_.start())
    tp.foreach(_.join())

    putBulk.teardown()
    teardown()
    import scala.concurrent.duration._
    val msg = s"this is the Controller   end at ${OwnUtils.getTimeNow()}. Elapsed ${(System.nanoTime() - beginTime).nanos.toSeconds} seconds"
    LOG.info(msg)
    msg
  }

}
