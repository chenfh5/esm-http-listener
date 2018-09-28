package io.github.chenfh5

import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.LoggerFactory

import scala.collection.mutable


class GetScroll(client: RestHighLevelClient, indexName: String, scrollSize: Int, queue: mutable.Queue[String]) extends Runnable {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchScrollRequest}
    import org.elasticsearch.common.unit.TimeValue
    import org.elasticsearch.search.Scroll
    import org.elasticsearch.search.builder.SearchSourceBuilder
    val scroll = new Scroll(TimeValue.timeValueMinutes(1L))

    val searchRequest = new SearchRequest(indexName)
    val searchSourceBuilder = new SearchSourceBuilder
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    // Note that from + size can not be more than the index.max_result_window index setting which defaults to 10,000(normal search)
    // @see https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-from-size.html
    // The size parameter allows you to configure the maximum number of hits to be returned with each batch of results(scroll search)
    // @see https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-scroll.html
    searchSourceBuilder.size(scrollSize)
    searchRequest.source(searchSourceBuilder)
    searchRequest.scroll(scroll)

    // first
    var loopCnt = 0
    LOG.info(s"loopCnt=$loopCnt scroll at: ${OwnUtils.getTimeNow()}")
    var searchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
    var scrollId = searchResponse.getScrollId
    var searchHits = searchResponse.getHits.getHits
    for (searchHit <- searchHits) {
      queue.enqueue(searchHit.getSourceAsString)
    }
    // loop
    while (searchHits != null && searchHits.length > 0) {
      loopCnt += 1
      LOG.info(s"loopCnt=$loopCnt scroll at: ${OwnUtils.getTimeNow()}")
      val scrollRequest = new SearchScrollRequest(scrollId)
      scrollRequest.scroll(scroll)
      searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT)
      scrollId = searchResponse.getScrollId
      searchHits = searchResponse.getHits.getHits
      for (searchHit <- searchHits) {
        queue.enqueue(searchHit.getSourceAsString)
      }
    }
    LOG.info(s"end loop scroll at: ${OwnUtils.getTimeNow()}")
    // teardown
    val clearScrollRequest = new ClearScrollRequest
    clearScrollRequest.addScrollId(scrollId)
    val clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
    val succeeded = clearScrollResponse.isSucceeded
    LOG.info(s"clearScrollResponse succeeded=$succeeded at: ${OwnUtils.getTimeNow()}")
  }

}

object GetScroll {
  def apply(client: RestHighLevelClient, indexName: String, scrollSize: Int = 10000, queue: mutable.Queue[String]): GetScroll = new GetScroll(client, indexName, scrollSize, queue)
}