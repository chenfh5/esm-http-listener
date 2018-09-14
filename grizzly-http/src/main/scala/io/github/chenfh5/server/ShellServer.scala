package io.github.chenfh5.server

import io.github.chenfh5.OwnConfigReader.OwnConfig
import io.github.chenfh5.handler.ShellHandler
import org.glassfish.grizzly.http.server.NetworkListener
import org.slf4j.LoggerFactory

import scala.util.Try

class ShellServer extends Server {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def init(): Unit = {
    LOG.info(s"G1Server host=${OwnConfig.SERVER_HOST}, port=${OwnConfig.HTTP_SERVER_PORT_1}")
    server.addListener(new NetworkListener("chenfh5 grizzly http ShellServer", OwnConfig.SERVER_HOST, OwnConfig.HTTP_SERVER_PORT_1))
    addHandler()
  }

  def addHandler(): Unit = {
    server.getServerConfiguration.addHttpHandler(ShellHandler(), "/" + "shell/esm")
  }

}

object ShellServer {
  private val LOG = LoggerFactory.getLogger(getClass)

  def apply(): ShellServer = new ShellServer()

  def main(args: Array[String]): Unit = {
    val shellServer = apply()
    shellServer.init()
    shellServer.start()

    // receive kill to graceful shutdown
    Try(scala.sys.addShutdownHook(shellServer.stop())).getOrElse {
      LOG.error("shutdown error")
      sys.exit(1)
    }

    // server always hold until receive kill signal
    Thread.currentThread.join()
  }

}
