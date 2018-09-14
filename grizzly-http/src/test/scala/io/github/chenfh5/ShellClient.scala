package io.github.chenfh5

import io.github.chenfh5.OwnConfigReader.OwnConfig
import scalaj.http.{Http, HttpResponse}

class ShellClient {

  def postEsmCmd(auth: String, json: String): HttpResponse[String] = {
    val resp = Http(url = "http://%s:%s/%s".format(OwnConfig.SERVER_HOST, OwnConfig.HTTP_SERVER_PORT_1, "shell/esm")).header("Authorization", auth).postData(json).method("POST").asString
    resp
  }

}

object ShellClient {
  def apply(): ShellClient = new ShellClient()

  def main(args: Array[String]): Unit = {
    val shellClient = ShellClient()
    val postBody =
      """
        {
          "source": "http://192.168.0.1:9200",
          "dest": "http://192.168.0.1:9201",
          "source_auth": "c5:c5",
          "dest_auth": "c5:c5",
          "src_indexes": "591_fuhaochen_2018070100",
          "shards": "9",
          "workers": "10",
          "bulk_size": "12",
          "count": "10000"
        }
      """
    val resp = shellClient.postEsmCmd(OwnConfig._AUTH64, postBody)
    println(resp.body)
  }

}
