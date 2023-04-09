package com.github.andersonreyes

import com.github.andersonreyes.api.Node
import com.github.andersonreyes.api.Message
import scala.io.StdIn.readLine
import scala.util.Try
import io.circe._, io.circe.generic.auto._, io.circe.syntax._, io.circe.parser._
import scala.util.Failure
import scala.util.Success
import com.github.andersonreyes.api.Message
import java.io.StringWriter
import java.io.PrintWriter
import com.github.andersonreyes.api.Body

object Main extends App {
  val node = new Node
  while (true) {
    Try {
      val line = readLine()
      // println(s"Line: $line")
      decode[Message](line) match {
        case Left(err) => {
          // println(s"ERror parsing line $line")
          // println(err.getMessage())
          // err.printStackTrace()

          val sw = new StringWriter()
          val pw = new PrintWriter(sw)

          err.printStackTrace(pw)

          val error = Message.ErrorMessage(
            "",
            "",
            Body.ErrorBody(
              -1,
              13,
              s"Failed to parse line $line with error: ${err
                .getMessage()}. and stack trace: ${sw.toString()}"
            )
          )

          println(error.asJson.noSpacesSortKeys)

        }
        case Right(value) => {
          val reply = node.handleMessage(value)
          println(reply.asJson.noSpacesSortKeys)
        }
      }
    }
  }

}
