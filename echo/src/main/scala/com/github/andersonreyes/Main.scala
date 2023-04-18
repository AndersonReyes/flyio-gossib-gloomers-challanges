package com.github.andersonreyes
import com.github.andersonreyes.api.Body
import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Node

import java.io.PrintWriter
import java.io.StringWriter
import scala.io.StdIn.readLine
import scala.util.Failure
import scala.util.Random
import scala.util.Success
import scala.util.Try

object Main extends App {

  val init: Try[InitMessage] = readLine().parseJson[InitMessage]

  init match {
    case Failure(exception) => {
      val err = ErrorMessage(
        "",
        "",
        Body.ErrorBody(
          -1,
          -1,
          s"failed to initialize server: ${exception.getMessage()}"
        )
      ).toJsonString

      println(err)
      System.exit(1)
    }

    case Success(value) => {
      val out = InitOkMessage(
        value.dest,
        value.src,
        Body.InitOk(value.body.msgId)
      )
      println(out.toJsonString)

      val node =
        Node.of(
          value.body.nodeId,
          // use random sample to build initial neighbors until we get a topology message
          Random
            .shuffle(value.body.nodeIds)
            .take(3)
        )

      node.serve
    }
  }

}
