package io.github.chenfh5.handler

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

    // TODO

  }


}

object ShellHandler {
  def apply(): ShellHandler = new ShellHandler()
}
