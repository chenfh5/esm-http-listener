package io.github.chenfh5

import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

import org.elasticsearch.action.bulk.{BackoffPolicy, BulkProcessor}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.{ByteSizeUnit, ByteSizeValue, TimeValue}
import org.slf4j.LoggerFactory

import scala.collection.mutable

class PutBulk(client: RestHighLevelClient, indexName: String, typeName: String, queue: mutable.Queue[String]) extends LifeCycle with Runnable {
  private val LOG = LoggerFactory.getLogger(getClass)
  private[this] var bulkProcessor: BulkProcessor = _

  override def setup(): Unit = {
    import org.elasticsearch.action.ActionListener
    import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
    import org.elasticsearch.client.RequestOptions

    val listener = new BulkProcessor.Listener() {
      override def beforeBulk(l: Long, bulkRequest: BulkRequest): Unit = {}

      override def afterBulk(l: Long, bulkRequest: BulkRequest, bulkResponse: BulkResponse): Unit = {}

      override def afterBulk(l: Long, bulkRequest: BulkRequest, throwable: Throwable): Unit = {
        LOG.warn("this is the PutBulk afterBulk err={}", throwable.getMessage)
      }
    }
    val bulkConsumer: BiConsumer[BulkRequest, ActionListener[BulkResponse]] = (bulkRequest: BulkRequest, actionListener: ActionListener[BulkResponse]) => client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, actionListener)
    val builder = BulkProcessor.builder(bulkConsumer, listener)

    // customer setting
    builder.setBulkActions(-1)
    // Start with a bulk size around 5–15 MB and slowly increase it until you do not see performance gains anymore
    // @see https://www.elastic.co/guide/en/elasticsearch/guide/current/indexing-performance.html#_using_and_sizing_bulk_requests
    builder.setBulkSize(new ByteSizeValue(15L, ByteSizeUnit.MB))
    builder.setConcurrentRequests(5) // Sets the number of concurrent requests allowed to be executed
    builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3))

    // real
    bulkProcessor = builder.build()
  }

  override def teardown(): Unit = {
    bulkProcessor.flush()
    bulkProcessor.awaitClose(30L, TimeUnit.SECONDS)
    bulkProcessor.close()
  }

  override def run(): Unit = {
    import org.elasticsearch.common.xcontent.XContentType
    var producerHasData = false
    while (!producerHasData) {
      LOG.info("this is the PutBulk to wait producer generate data")
      Thread.sleep(1000)
      if (queue.nonEmpty) producerHasData = true
    }

    // execute when have data
    while (queue.nonEmpty) {
      bulkProcessor.add(new IndexRequest(indexName, typeName).source(queue.dequeue(), XContentType.JSON))
    }
  }

}

object PutBulk {
  def apply(client: RestHighLevelClient, indexName: String, typeName: String, queue: mutable.Queue[String]): PutBulk = new PutBulk(client, indexName, typeName, queue)
}
