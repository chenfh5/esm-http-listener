package io.github.chenfh5.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.OwnUtils
import org.glassfish.grizzly.http.server.{Request, Response}
import org.slf4j.LoggerFactory

class ShellHandler extends HandlerTrait {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def doGet(request: Request, response: Response): Unit = ???

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

    val esmBin = OwnUtils.makeFile(OwnUtils.getCurrentDir, "esmdir", "esm")

    // TODO: nohup shell common?
    val cmd =
      """sh %s --source=%s --dest=%s --source_auth=%s --dest_auth=%s --src_indexes=%s
        | --copy_settings --copy_mappings --refresh --sliced_scroll_size=5 --shards=%s --workers=%s --bulk_size=%s --count=%s""".stripMargin
        .format(esmBin, source, dest, source_auth, dest_auth, src_indexes, shards, workers, bulk_size, count)

    LOG.info("this is the ShellHandler cmd={}", cmd)
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString)
    response.getWriter.write(s"cmd=$cmd, trigger at ${OwnUtils.getTimeNow()}")
    response.finish()
  }

}

object ShellHandler {
  def apply(): ShellHandler = new ShellHandler()
}
