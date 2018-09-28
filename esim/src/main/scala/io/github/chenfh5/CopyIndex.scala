package io.github.chenfh5

import org.apache.http.util.EntityUtils
import org.elasticsearch.client.{Request, RestHighLevelClient}
import org.slf4j.LoggerFactory

class CopyIndex {
  private val LOG = LoggerFactory.getLogger(getClass)

  private[this] def getSettingAndMapping(client: RestHighLevelClient, indexName: String, typeName: String): (Int, String) = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val endpoint = "/%s/_mapping/%s".format(indexName, typeName)
    val mappingMap = parse(EntityUtils.toString(client.getLowLevelClient.performRequest(new Request("GET", endpoint)).getEntity)).extract[Map[String, JValue]]

    val settingStr = EntityUtils.toString(client.getLowLevelClient.performRequest(new Request("GET", "/%s/_settings".format(indexName))).getEntity)
    val settingMap = parse(settingStr).extract[Map[String, Map[String, Map[String, Map[String, Any]]]]]
    val shardNum = settingMap(indexName)("settings")("index")("number_of_shards")

    (shardNum.toString.toInt, compact(mappingMap.get(indexName).head))
  }

  def copy(srcClient: RestHighLevelClient, destClient: RestHighLevelClient, srcIndexName: String, typeName: String, destIndexName: String): Boolean = {
    // merge setting and mapping into source
    val (shardNum, mappingStr) = getSettingAndMapping(srcClient, srcIndexName, typeName)
    val settingStr =s""""settings" : {"number_of_shards" : $shardNum},"""
    val source = Seq("{", settingStr, mappingStr.drop(1).dropRight(1), "}").mkString

    // process
    val endpoint = "/%s".format(destIndexName)
    try {
      destClient.getLowLevelClient.performRequest(new Request("DELETE", endpoint))
      LOG.info("this is the CopyIndex DELETE index={}", endpoint)
      Thread.sleep(5001)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }

    val request = new Request("PUT", endpoint)
    request.setJsonEntity(source)
    val response = destClient.getLowLevelClient.performRequest(request)
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val res = parse(EntityUtils.toString(response.getEntity)).extract[Map[String, Boolean]]
    LOG.info("this is the CopyIndex CREATE index={}", endpoint)
    Thread.sleep(5001)
    res("acknowledged") && res("shards_acknowledged")
  }

}

object CopyIndex {
  def apply(): CopyIndex = new CopyIndex()
}
