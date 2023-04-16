package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import io.circe.generic.extras._
import cats.syntax.functor._
import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
import io.circe.syntax._
import io.circe.HCursor
import io.circe.JsonObject
import java.util.Base64
import io.circe.DecodingFailure

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

  @ConfiguredJsonCodec case class GenerateMessage(
      src: String,
      dest: String,
      body: Generate
  ) extends Message

  @ConfiguredJsonCodec case class GenerateMessageOk(
      src: String,
      dest: String,
      body: GenerateOk
  ) extends Message

  @ConfiguredJsonCodec case class TopologyMessage(
      src: String,
      dest: String,
      body: Topology
  ) extends Message

  @ConfiguredJsonCodec case class TopologyOkMessage(
      src: String,
      dest: String,
      body: TopologyOk
  ) extends Message

  @ConfiguredJsonCodec case class BroadcastMessage(
      src: String,
      dest: String,
      body: Broadcast
  ) extends Message

  @ConfiguredJsonCodec case class BroadcastOkMessage(
      src: String,
      dest: String,
      body: BroadcastOk
  ) extends Message

  @ConfiguredJsonCodec case class ReadMessage(
      src: String,
      dest: String,
      body: Read
  ) extends Message

  @ConfiguredJsonCodec case class ReadOkMessage(
      src: String,
      dest: String,
      body: ReadOk
  ) extends Message

//   implicit val decodeMessage: Decoder[Message] = List[Decoder[Message]](
//     Decoder[EchoMessage].widen,
//     Decoder[InitMessage].widen,
//     Decoder[GenerateMessage].widen,
//     Decoder[TopologyMessage].widen,
//     Decoder[BroadcastMessage].widen,
//     Decoder[ReadMessage].widen
//   ).reduceLeft(_ or _)

  implicit val decodeMessage: Decoder[Message] = Decoder.instance(h => {
    h.downField("body")
      .downField("type")
      .as[String]
      .flatMap {
        case "echo" =>
          Decoder[EchoMessage].tryDecode(h)
        case "init"      => Decoder[InitMessage].tryDecode(h)
        case "generate"  => Decoder[GenerateMessage].tryDecode(h)
        case "topology"  => Decoder[TopologyMessage].tryDecode(h)
        case "broadcast" => Decoder[BroadcastMessage].tryDecode(h)
        case "read"      => Decoder[ReadMessage].tryDecode(h)
        case invalid =>
          Left(DecodingFailure(s"invalid body type $invalid", h.history))
      }
  })

  implicit val encodeMessage: Encoder[Message] = Encoder.instance {
    case echoOk: EchoOkMessage => echoOk.asJson
    case initOk: InitOkMessage => initOk.asJson
    case g: GenerateMessageOk  => g.asJson
    case t: TopologyOkMessage  => t.asJson
    case b: BroadcastOkMessage => b.asJson
    case r: ReadOkMessage      => r.asJson
  }
}
