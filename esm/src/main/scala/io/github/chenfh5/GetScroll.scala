package io.github.chenfh5

import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders

class GetScroll(client: RestHighLevelClient) {

  def get(indexToGet: String, scrollSize: Int = 10000): Unit = {
    import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchScrollRequest}
    import org.elasticsearch.common.unit.TimeValue
    import org.elasticsearch.search.Scroll
    import org.elasticsearch.search.builder.SearchSourceBuilder
    val scroll = new Scroll(TimeValue.timeValueMinutes(1L))

    val searchRequest = new SearchRequest(indexToGet)
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
    println(s"loopCnt=$loopCnt scroll at: ${OwnUtils.getTimeNow()}")
    var searchResponse = client.search(searchRequest)
    var scrollId = searchResponse.getScrollId
    var searchHits = searchResponse.getHits.getHits
    for (searchHit <- searchHits) {
      println(Map(searchHit.getId -> searchHit.getSourceAsString))
    }
    // loop
    println(s"begin loop scroll at: ${OwnUtils.getTimeNow()}")
    while (searchHits != null && searchHits.length > 0) {
      loopCnt += 1
      println(s"loopCnt=$loopCnt scroll at: ${OwnUtils.getTimeNow()}")
      val scrollRequest = new SearchScrollRequest(scrollId)
      scrollRequest.scroll(scroll)
      searchResponse = client.searchScroll(scrollRequest)
      scrollId = searchResponse.getScrollId
      searchHits = searchResponse.getHits.getHits
      for (searchHit <- searchHits) {
        println(Map(searchHit.getId -> searchHit.getSourceAsString))
      }
    }
    println(s"end loop scroll at: ${OwnUtils.getTimeNow()}")
    // teardown
    val clearScrollRequest = new ClearScrollRequest
    clearScrollRequest.addScrollId(scrollId)
    val clearScrollResponse = client.clearScroll(clearScrollRequest)
    val succeeded = clearScrollResponse.isSucceeded
    println(s"clearScrollResponse succeeded=$succeeded at: ${OwnUtils.getTimeNow()}")
  }

}

object GetScroll {
  def apply(client: RestHighLevelClient): GetScroll = new GetScroll(client)
}
