package io.github.chenfh5


import scala.sys.process._

object TestOnly {

  // java -cp grizzly-http.jar io.github.chenfh5.TestOnly 1
  // nohup command > myout.file 2>&1 &
  def main(args: Array[String]): Unit = {

    val flag = args.head
    val cmd ="""/data/home/fuhaochen/esm-http-listener/script/../esmdir/esm --source=http://100.95.130.81:6300 --dest=http://10.50.118.61:6300 --source_auth=blueking:bLuEkinG@2018 --dest_auth=blueking:bLuEkinG@2018 --src_indexes=821_etl_log_testenv_0709_sw_2018080300 --copy_settings --copy_mappings --refresh --sliced_scroll_size=5 --shards=7 --workers=10 --bulk_size=12 --count=10000"""

    flag.toInt match {
      case 1 => (cmd #>> new java.io.File(s"/data/home/fuhaochen/esm-http-listener/lib/t1.log")).run()
      case 2 => (cmd #>> new java.io.File(s"/data/home/fuhaochen/esm-http-listener/lib/t2.log")).!!
    }


    println("end here " + OwnUtils.getTimeNow())

  }

  def t1() = {
    import java.io.{File, PrintWriter}

    import scala.sys.process._
    var cmd ="""/home/fuhaochen-ub/Downloads/esmdir/esm --source=http://10.213.147.222:8080 --dest=http://10.229.140.207:8080 --source_auth=blueking:bLuEkinG@2018 --dest_auth=blueking:bLuEkinG@2018 --src_indexes=100183_etl_frame_sms_2018073000 --copy_settings --copy_mappings --refresh --sliced_scroll_size=5 --shards=7 --workers=10 --bulk_size=12 --count=10000"""
//    cmd ="""date"""
    val s1= cmd.run()
//    println(s1.mkString(""))
//    println(s1.mkString(""))
println(s1)

  }

}


///data/home/fuhaochen/esm-http-listener/esmdir/esm --source=http://10.213.147.222:8080 --dest=http://10.229.140.207:8080 --source_auth=blueking:bLuEkinG@2018 --dest_auth=blueking:bLuEkinG@2018 --src_indexes=100183_etl_frame_sms_2018073000