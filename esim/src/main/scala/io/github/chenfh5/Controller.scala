package io.github.chenfh5

import java.util.concurrent.atomic.AtomicLong

import io.github.chenfh5.OwnConfigReader.OwnConfig
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory

import scala.collection.mutable

class Controller(srcHost: String, srcPort: Int, destHost: String, destPort: Int) extends LifeCycle {
  private val LOG = LoggerFactory.getLogger(getClass)
  var srcClient: RestHighLevelClient = _
  var destClient: RestHighLevelClient = _
  val queue: mutable.Queue[String] = scala.collection.mutable.Queue[String]()
  var remainCnt = new AtomicLong(0)

  override def setup(): Unit = {
    import org.apache.http.HttpHost
    import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
    import org.apache.http.impl.client.BasicCredentialsProvider
    import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
    import org.elasticsearch.client.RestClient
    val credentialsProvider = new BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(OwnUtils.decode(OwnConfig._ESUSER), OwnUtils.decode(OwnConfig._ESPASSWORD)))

    srcClient = new RestHighLevelClient(RestClient.builder(new HttpHost(srcHost, srcPort, "http")).setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) => httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)))
    destClient = new RestHighLevelClient(RestClient.builder(new HttpHost(destHost, destPort, "http")).setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) => httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)))
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  override def teardown(): Unit = {
    srcClient.getLowLevelClient.close()
    destClient.getLowLevelClient.close()
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  def process(srcIndexName: String, typeName: String, destIndexName: String, scrollSize: Int = 10000): Unit = {
    LOG.info(s"this is the Controller begin at ${OwnUtils.getTimeNow()}")
    setup()
    val copyIndex = CopyIndex()
    val res = copyIndex.copy(srcClient, destClient, srcIndexName, typeName, destIndexName)
    if (!res) throw new RuntimeException("create copy index fail")

    // producer
    val getScroll = GetScroll(srcClient, srcIndexName, scrollSize, queue)
    // consumer
    val putBulk = PutBulk(destClient, destIndexName, typeName, queue)
    putBulk.setup()
    // reporter
    val countQueueSize = new CountQueueSize(queue)

    // start thread
    // @see https://stackoverflow.com/questions/20495414/thread-join-equivalent-in-executor
    val tp = Seq(new Thread(getScroll), new Thread(putBulk), new Thread(countQueueSize))
    tp.foreach(_.start())
    tp.foreach(_.join())

    putBulk.teardown()
    teardown()
    LOG.info(s"this is the Controller2   end at ${OwnUtils.getTimeNow()}")
  }

}

object Controller {
  def apply(srcHost: String, srcPort: Int, destHost: String, destPort: Int): Controller = new Controller(srcHost, srcPort, destHost, destPort)
}
