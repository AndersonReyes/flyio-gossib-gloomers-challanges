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

  node.serve

}
