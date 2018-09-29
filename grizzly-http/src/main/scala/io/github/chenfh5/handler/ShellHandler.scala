package io.github.chenfh5.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.{Controller, OwnUtils}
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
    var msg: String = ""
    try {
      val srcHost = postBodyMap("srcHost")
      val srcPort = postBodyMap("srcPort").toInt
      val destHost = postBodyMap("destHost")
      val destPort = postBodyMap("destPort").toInt
      val authUser = postBodyMap("authUser")
      val authPW = postBodyMap("authPW")
      val srcIndexName = postBodyMap("srcIndexName")
      val srcTypeName = postBodyMap("srcTypeName")
      val destIndexName = postBodyMap("destIndexName")
      val scrollSize = postBodyMap("scrollSize").toInt
      val concurrentRequests = postBodyMap("concurrentRequests").toInt
      val controller = new Controller(srcHost, srcPort, destHost, destPort, authUser, authPW)
      msg = controller.process(srcIndexName, srcTypeName, destIndexName, scrollSize, concurrentRequests)
      // TODO: catch http request abort to close the controller
    } catch {
      case e: Throwable =>
        msg = s"esim error=${e.getMessage}"
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
