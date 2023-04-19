package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import io.circe._
import io.circe.generic.extras._
import io.circe.parser._
import io.circe.syntax._

import java.util.Base64
import scala.util.Try

trait Message {
  val src: String
  val dest: String
}

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
        // Since nodes can send broadcast ok then we need to be able to receive/decode
        case "broadcast_ok" => Decoder[BroadcastOkMessage].tryDecode(h)
        case "read"         => Decoder[ReadMessage].tryDecode(h)
        case invalid =>
          Left(DecodingFailure(s"invalid body type $invalid", h.history))
      }
  })

  implicit val encodeMessage: Encoder[Message] = Encoder.instance {
    // Nodes can generate broadcast messages themselves
    case broadcast: BroadcastMessage =>
      broadcast.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "broadcast".asJson))))
        .top
        .get
    case echoOk: EchoOkMessage =>
      echoOk.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "echo_ok".asJson))))
        .top
        .get
    case initOk: InitOkMessage =>
      initOk.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "init_ok".asJson))))
        .top
        .get
    case g: GenerateMessageOk =>
      g.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "generate_ok".asJson))))
        .top
        .get
    case t: TopologyOkMessage =>
      t.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "topology_ok".asJson))))
        .top
        .get
    case b: BroadcastOkMessage =>
      b.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "broadcast_ok".asJson))))
        .top
        .get
    case r: ReadOkMessage =>
      r.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "read_ok".asJson))))
        .top
        .get

    case err: ErrorMessage =>
      err.asJson.hcursor
        .downField("body")
        .withFocus(_.mapObject(g => g.+:(("type", "error".asJson))))
        .top
        .get
  }

  implicit class MassageFromString(s: String) {
    def parseJson[T <: Message: Decoder]: Try[T] =
      Try { decode[T](s) }.flatMap(_.toTry)
  }

  implicit class MessageImplicits(m: Message) {
    def toJsonString: String = m.asJson(encodeMessage).noSpacesSortKeys
  }

}
