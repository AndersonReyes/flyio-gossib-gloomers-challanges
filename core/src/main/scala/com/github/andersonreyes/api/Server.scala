package com.github.andersonreyes.api

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
import scala.collection.mutable.Queue
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Server {
  def handleMessage(msg: Message): Message
  def handleError(line: String, err: Throwable): Message = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)

    err.printStackTrace(pw)

    Message.ErrorMessage(
      "",
      "",
      Body.ErrorBody(
        -1,
        13,
        s"Failed to parse line $line with error: ${err
          .getMessage()}. and stack trace: ${sw.toString()}"
      )
    )
  }

  def serve: Unit = {
    // val workers = new Queue[Future[_]](100)

    while (true) {
      val line = readLine()

      Future {
        decode[Message](line).flatMap(m =>
          Try(handleMessage(m).asJson.noSpacesSortKeys).toEither
        )
      }.map { handled =>
        handled match {
          case Left(err) => {
            val errMsg = handleError(line, err)
            println(errMsg.asJson.noSpacesSortKeys)

          }
          case Right(value) => println(value)
        }
      }
    }
  }
}
