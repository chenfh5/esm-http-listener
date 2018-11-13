package io.github.chenfh5.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.{Controller, OwnUtils, Utils}
import org.glassfish.grizzly.http.server.{Request, Response}
import org.slf4j.LoggerFactory

class ShellHandler extends HandlerTrait {
  private val LOG = LoggerFactory.getLogger(getClass)

  /**
    * @api {GET} shell/esm get running tasks
    * @apiDescription thread-name format is [type_timestamp_params.hashcode], e.g., countQueueSize_1542095780885_1468583655
    * @apiGroup ESIM
    * @apiSuccessExample {String} Success-Response:
    *                    get health is success, trigger at 2018-11-13 16:11:21 Tue, runningTasks=Set(countQueueSize_1542095780885_1468583655, countQueueSize_1542095777669_-525499890)
    */
  override def doGet(request: Request, response: Response): Unit = {
    LOG.debug("this is the ShellHandler doGet")
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString)
    import collection.JavaConverters._
    val runningTasks = Thread.getAllStackTraces.keySet.asScala.map(_.getName).filter(_.contains("countQueueSize"))
    response.getWriter.write(s"get health is success, trigger at ${OwnUtils.getTimeNow()}, runningTasks=$runningTasks")
    response.finish()
  }

  /**
    * @api {DELETE} shell/esm cancel specified task
    * @apiGroup ESIM
    * @apiParam {String} id
    * @apiParamExample {String} Request-Example:
    *                  shell/esm?id=7947866
    * @apiSuccessExample {String} Success-Response:
    *                    remove task with id=7947866, size=3
    */
  override def doDelete(request: Request, response: Response): Unit = {
    LOG.debug("this is the ShellHandler doDelete")
    val id = request.getParameter("id")
    import collection.JavaConverters._
    val threads = Thread.getAllStackTraces.keySet.asScala.filter(_.getName.contains(id))
    threads.foreach(_.interrupt())
    response.getWriter.write(s"remove task with id=$id, size=${threads.size}")
    response.finish()
  }

  /**
    * @api {POST} shell/esm create specified task
    * @apiGroup ESIM
    * @apiParamExample {json} Request-Example:
    *                  {
    *                  "srcHost": "localhost",
    *                  "srcPort": "8080",
    *                  "destHost": "localhost",
    *                  "destPort": "8082",
    *                  "authUser": "Ymx1ZWtpbmc=",
    *                  "authPW": "Ykx1RWtpbkdAMjAxOA==",
    *                  "srcIndexName": "src_index_name",
    *                  "srcTypeName": "src_index_type_name",
    *                  "destIndexName": "dest_index_name",
    *                  "scrollSize": "10000",
    *                  "concurrentRequests": "10"
    *                  }
    * @apiSuccessExample {json} Success-Response:
    *                    blocking the request until response, but you can close the session whose task had runned in backgroud.
    *                    If you want to kill it, using `GET` to find the id, and then using `DELETE` to cancel it
    */
  override def doPost(request: Request, response: Response): Unit = {
    LOG.debug("this is the ShellHandler doPost")
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    val postBodyStr = scala.io.Source.fromInputStream(request.getInputStream).mkString
    val postBodyMap = parse(postBodyStr).extract[Map[String, String]]
    val threadUniqueId = postBodyMap.hashCode()
    var msg: String = ""
    if (Utils.isTaskAlreadyExist(threadUniqueId)) msg = s"task with params=$postBodyMap, id=$threadUniqueId have already exist" // task already exist, do not submit again
    else { // submit task
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
      msg = controller.process(srcIndexName, srcTypeName, destIndexName, scrollSize, concurrentRequests, threadUniqueId)
    }
    // return response
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString)
    response.getWriter.write(s"msg=$msg, trigger at ${OwnUtils.getTimeNow()}")
    response.finish()
  }

}

object ShellHandler {
  def apply(): ShellHandler = new ShellHandler()
}
