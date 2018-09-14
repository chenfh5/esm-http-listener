package io.github.chenfh5.server

import io.github.chenfh5.OwnConfigReader.OwnConfig
import io.github.chenfh5.handler.ShellHandler
import org.apache.commons.cli.{CommandLine, DefaultParser, Options}
import org.apache.commons.lang3.StringUtils
import org.glassfish.grizzly.http.server.NetworkListener
import org.slf4j.LoggerFactory

import scala.util.Try

class ShellServer(host: String, port: Int) extends Server {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def init(): Unit = {
    LOG.info(s"G1Server host=$host, port=$port")
    server.addListener(new NetworkListener("chenfh5 grizzly http ShellServer", host, port))
    addHandler()
  }

  def addHandler(): Unit = {
    server.getServerConfiguration.addHttpHandler(ShellHandler(), "/" + "shell/esm")
  }

}

object ShellServer {
  private val LOG = LoggerFactory.getLogger(getClass)

  def apply(host: String, port: Int): ShellServer = new ShellServer(host, port)

  def main(args: Array[String]): Unit = {
    val cli = parseArgs2Cli(args)
    val host = cli.getOptionValue("host", OwnConfig.SERVER_HOST)
    val port = cli.getOptionValue("port", OwnConfig.HTTP_SERVER_PORT_1.toString)
    OwnConfig.ESM_BIN_FILE = cli.getOptionValue("esm")
    require(StringUtils.isNoneBlank(OwnConfig.ESM_BIN_FILE))

    val shellServer = apply(host, port.toInt)
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

  def parseArgs2Cli(args: Array[String]): CommandLine = {
    try {
      val options = new Options()
        .addOption(null, "host", true, "server host")
        .addOption(null, "port", true, "server port")
        .addOption(null, "esm", true, "absolute path of esm bin file")
      new DefaultParser().parse(options, args)
    }
    catch {
      case e: Throwable =>
        e.printStackTrace()
        throw new Error("Failed to parse command line options")
    }
  }

}
