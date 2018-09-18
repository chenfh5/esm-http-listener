package io.github.chenfh5.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.OwnConfigReader.OwnConfig
import io.github.chenfh5.OwnUtils
import org.apache.commons.lang3.StringUtils
import org.glassfish.grizzly.http.server.{Request, Response}
import org.slf4j.LoggerFactory

class ShellHandler extends HandlerTrait {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def doGet(request: Request, response: Response): Unit = {
    LOG.debug("this is the ShellHandler doPost")
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString)
    response.getWriter.write(s"get health is success, trigger at ${OwnUtils.getTimeNow()}")
    response.finish()
  }

  override def doDelete(request: Request, response: Response): Unit = ???

  override def doPost(request: Request, response: Response): Unit = {
    import scala.sys.process._
    LOG.debug("this is the ShellHandler doPost")
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val postBodyStr = scala.io.Source.fromInputStream(request.getInputStream).mkString
    val postBodyMap = parse(postBodyStr).extract[Map[String, String]]

    val source = postBodyMap("source")
    val dest = postBodyMap("dest")
    val source_auth = postBodyMap("source_auth")
    val dest_auth = postBodyMap("dest_auth")
    val src_indexes = postBodyMap("src_indexes")
    val shards = postBodyMap("shards")
    val workers = postBodyMap.getOrElse("workers", 10)
    val bulk_size = postBodyMap.getOrElse("bulk_size", 12)
    val count = postBodyMap.getOrElse("count", 10000)
    val iskill = postBodyMap.getOrElse("iskill", "false").toBoolean

    var cmd =
      """%s/esm --source=%s --dest=%s --source_auth=%s --dest_auth=%s --src_indexes=%s --copy_settings --copy_mappings --refresh --sliced_scroll_size=5 --shards=%s --workers=%s --bulk_size=%s --count=%s"""
        .format(OwnConfig.ESM_BIN_DIR, source, dest, source_auth, dest_auth, src_indexes, shards, workers, bulk_size, count)
    LOG.info("this is the ShellHandler cmd={}", cmd)
    var msg: String = ""

    try {
      if (iskill) {
        ("ps aux" #| Process(Seq("grep", cmd)) #| Process(Seq("grep", "-v", "grep")) #| Process(Seq("awk", "{print $2}")) #| "xargs kill -9").!! // start sync
        msg = s"cmd=$cmd, kill success"
      }
      else {
        // check cmd exists or not
        val pid = ("ps aux" #| Process(Seq("grep", cmd)) #| Process(Seq("grep", "-v", "grep")) #| Process(Seq("awk", "{print $2}"))).!!.trim()
        if (StringUtils.isNoneBlank(pid)) msg = s"cmd=$cmd, already exist, pleas wait until its finish. To terminate the cmd, try post with kill=true" // exists, no longer run again
        else {
          // not exists
          val name = s"${OwnUtils.getTimeNow(true)}_${cmd.hashCode}"
          val simpleProcess = (cmd #>> new java.io.File(s"${OwnConfig.ESM_BIN_DIR}/log/$name")).run() // start async and redirect
          msg = s"cmd=$cmd, running in backgound with simpleProcess=$simpleProcess"
        }
      }
    } catch {
      case e: Throwable =>
        msg = s"cmd=$cmd, error=${e.getMessage}"
        LOG.error(s"this is the $msg")
    } finally {
      response.setCharacterEncoding(StandardCharsets.UTF_8.toString)
      response.getWriter.write(s"msg=$msg, trigger at ${OwnUtils.getTimeNow()}")
      response.finish()
    }
  }
}

object ShellHandler {
  def apply(): ShellHandler = new ShellHandler()
}
