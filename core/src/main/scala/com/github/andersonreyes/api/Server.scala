package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body
import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Node
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import scala.io.StdIn.readLine
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import cats.data.Op

trait Server {
  def handleMessage(msg: Message): Option[Message]

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

    while (true) {
      val line = readLine()

      val f: Try[Option[Message]] = line.parseJson[Message].map(handleMessage)

      f match {
        case Failure(err) => {
          val errMsg = handleError(line, err)
          val msg = errMsg.asJson.noSpacesSortKeys
          println(msg)

        }
        case Success(value) => value.map(_.toJsonString).foreach(println)
      }
    }
  }
}
