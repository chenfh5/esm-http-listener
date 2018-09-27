package io.github.chenfh5

import org.elasticsearch.action.bulk.BulkProcessor
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.testng.annotations.{AfterClass, BeforeClass, Test}

class FuncTest {
  private val LOG = LoggerFactory.getLogger(getClass)
  private[this] var clientInit: ClientInit = _
  private[this] var getBulk: PutBulk = _

  var client: RestHighLevelClient = _
  var bulkProcessor: BulkProcessor = _

  val (host, port) = ("10.229.140.207", 8080)
  val esindex = "591_etl_bkdata_bad_msg_2018082100"
  val estype = "etl_bkdata_bad_msg"
  val esId = "AWYZHMqjkeHQrb9HdUNB"

  @BeforeClass
  def setUp(): Unit = {
    clientInit = ClientInit(host, port)
    clientInit.setup()
    client = clientInit.get()

    getBulk = PutBulk(client)
    getBulk.setup()
    bulkProcessor = getBulk.get()
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  @AfterClass
  def shut(): Unit = {
    getBulk.teardown()
    clientInit.teardown()
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  @Test(enabled = true)
  def testGetResponse(): Unit = {
    val getRequest = new GetRequest(esindex, estype, esId)
    val getResponse = client.get(getRequest)
    if (getResponse.isExists) {
      val version = getResponse.getVersion
      val sourceAsString = getResponse.getSourceAsString
      val sourceAsMap = getResponse.getSourceAsMap
      val sourceAsBytes = getResponse.getSourceAsBytes

      println(version)
      println(sourceAsString)
    }
  }

  @Test(enabled = true)
  def testGet(): Unit = {
    val getScroll = GetScroll(client)
    getScroll.get(esindex)
  }

  def testPut(): Unit = {
    import org.elasticsearch.action.index.IndexRequest
    import org.elasticsearch.common.xcontent.XContentType
    val one = new IndexRequest("posts", "doc").source(XContentType.JSON, "title", "In which order are my Elasticsearch queries executed?")
    val two = new IndexRequest("posts", "doc").source(XContentType.JSON, "title", "Current status and upcoming changes in Elasticsearch")
    val three = new IndexRequest("posts", "doc").source(XContentType.JSON, "title", "The Future of Federated Search in Elasticsearch")

    bulkProcessor.add(one)
    bulkProcessor.add(two)
    bulkProcessor.add(three)

    bulkProcessor.close()
  }
}
