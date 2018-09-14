package io.github.chenfh5.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.OwnConfigReader.OwnConfig
import io.github.chenfh5.OwnUtils
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

    var cmd =
      """%s/esm --source=%s --dest=%s --source_auth=%s --dest_auth=%s --src_indexes=%s --copy_settings --copy_mappings --refresh --sliced_scroll_size=5 --shards=%s --workers=%s --bulk_size=%s --count=%s"""
        .format(OwnConfig.ESM_BIN_DIR, source, dest, source_auth, dest_auth, src_indexes, shards, workers, bulk_size, count)
    LOG.info("this is the ShellHandler cmd={}", cmd)

    // add nohup
    val name = s"${OwnUtils.getTimeNow(true)}_${cmd.hashCode}"
    cmd += s""" >${OwnConfig.ESM_BIN_DIR}/log/$name 2>&1 &""" // log file for each http call

    // run shell script
    var msg: String = ""
    try {
      import sys.process._
      cmd.!! // TODO: blocking here, need async. 2 how to pipeline make file
      msg = s"cmd=$cmd success"
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
