package io.github.chenfh5

import java.util.function.BiConsumer

import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.{BackoffPolicy, BulkProcessor}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.{ByteSizeUnit, ByteSizeValue, TimeValue}
import org.elasticsearch.threadpool.ThreadPool

class PutBulk(client: RestHighLevelClient) extends LifeCycle {
  private[this] var bulkProcessor: BulkProcessor = _

  override def setup(): Unit = {
    import org.elasticsearch.action.bulk.{BulkProcessor, BulkRequest, BulkResponse}
    import org.elasticsearch.common.settings.Settings
    import org.elasticsearch.node.Node

    // @see https://discuss.elastic.co/t/headache-with-resthighlevelclient-and-bulkprocessor/106072/2
    val threadPool = new ThreadPool(Settings.builder.put(Node.NODE_NAME_SETTING.getKey, "high-level-client").build)

    // Java 8 functions interface
    // @see https://stackoverflow.com/a/52061477
    val bulkAsyncAsJava: BiConsumer[BulkRequest, ActionListener[BulkResponse]] = (bulkRequest: BulkRequest, actionListener: ActionListener[BulkResponse]) => {
      client.bulkAsync(bulkRequest, actionListener)
    }
    val builder = new BulkProcessor.Builder(
      bulkAsyncAsJava,
      new BulkProcessor.Listener {
        override def beforeBulk(l: Long, bulkRequest: BulkRequest): Unit = ???

        override def afterBulk(l: Long, bulkRequest: BulkRequest, bulkResponse: BulkResponse): Unit = ???

        override def afterBulk(l: Long, bulkRequest: BulkRequest, throwable: Throwable): Unit = ???
      },
      threadPool)
    // customer setting
    builder.setBulkActions(-1)
    // Start with a bulk size around 5–15 MB and slowly increase it until you do not see performance gains anymore
    // @see https://www.elastic.co/guide/en/elasticsearch/guide/current/indexing-performance.html#_using_and_sizing_bulk_requests
    builder.setBulkSize(new ByteSizeValue(15L, ByteSizeUnit.MB))
    // Sets the number of concurrent requests allowed to be executed
    builder.setConcurrentRequests(5)
    builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3))


    // real
    bulkProcessor = builder.build()
  }

  override def teardown(): Unit = {
    bulkProcessor.close()
  }

  def get(): BulkProcessor = bulkProcessor

}

object PutBulk {
  def apply(client: RestHighLevelClient): PutBulk = new PutBulk(client)
}
