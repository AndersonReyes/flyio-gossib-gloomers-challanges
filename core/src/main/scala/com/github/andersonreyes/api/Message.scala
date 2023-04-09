package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import io.circe.generic.extras._
import cats.syntax.functor._
import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
import io.circe.syntax._

trait Message

object Message {
  import com.github.andersonreyes.api.Config._

  @ConfiguredJsonCodec case class EchoMessage(
      src: String,
      dest: String,
      body: Echo
  ) extends Message

  @ConfiguredJsonCodec case class EchoOkMessage(
      src: String,
      dest: String,
      body: EchoOk
  ) extends Message

  @ConfiguredJsonCodec case class InitMessage(
      src: String,
      dest: String,
      id: Int,
      body: Init
  ) extends Message

  @ConfiguredJsonCodec case class InitOkMessage(
      src: String,
      dest: String,
      body: InitOk
  ) extends Message

  @ConfiguredJsonCodec case class ErrorMessage(
      src: String,
      dest: String,
      body: ErrorBody
  ) extends Message

  implicit val decodeMessage: Decoder[Message] = List[Decoder[Message]](
    Decoder[EchoMessage].widen,
    // Decoder[EchoOkMessage].widen,
    Decoder[InitMessage].widen
    // Decoder[InitOkMessage].widen
  ).reduceLeft(_ or _)

  implicit val encodeMessage: Encoder[Message] = Encoder.instance {
    case echo: EchoMessage     => echo.asJson
    case echoOk: EchoOkMessage => echoOk.asJson
    case init: InitMessage     => init.asJson
    case initOk: InitOkMessage => initOk.asJson
    case err: ErrorMessage     => err.asJson
  }
}
