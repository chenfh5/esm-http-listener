package io.github.chenfh5

import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestHighLevelClient}

class ClientInit(host: String, port: Int) extends LifeCycle {
  private[this] var lowLevelRestClient: RestClient = _
  private[this] var client: RestHighLevelClient = _


  override def setup(): Unit = {
    lowLevelRestClient = RestClient.builder(new HttpHost(host, port, "http")).build()
    client = new RestHighLevelClient(lowLevelRestClient)
  }

  override def teardown(): Unit = {
    lowLevelRestClient.close()
  }

  def get(): RestHighLevelClient = client

}

object ClientInit {
  def apply(host: String, port: Int): ClientInit = new ClientInit(host, port)
}
