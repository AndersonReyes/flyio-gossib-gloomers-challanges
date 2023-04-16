package com.github.andersonreyes.api

import io.circe.generic.extras._

object Body {
  import com.github.andersonreyes.api.Config._

  @ConfiguredJsonCodec case class Echo(
      msgId: Int,
      echo: String
  )
  @ConfiguredJsonCodec case class EchoOk(
      msgId: Int,
      echo: String,
      @JsonKey("in_reply_to") replyTo: Int
  )

  @ConfiguredJsonCodec case class Init(
      msgId: Int,
      nodeId: String,
      nodeIds: List[String]
  )
  @ConfiguredJsonCodec case class InitOk(
      @JsonKey("in_reply_to") replyTo: Int
  )

  @ConfiguredJsonCodec case class ErrorBody(
      @JsonKey("in_reply_to") replyTo: Int,
      code: Int,
      text: String
  )

  @ConfiguredJsonCodec case class Generate(
      msgId: Int
  )
  @ConfiguredJsonCodec case class Topology(
      topology: Map[String, List[String]],
      msgId: Int
  )

  @ConfiguredJsonCodec case class TopologyOk(
      msgId: Int
  )
  @ConfiguredJsonCodec case class GenerateOk(
      id: String,
      msgId: Int,
      @JsonKey("in_reply_to") replyTo: Int
  )

  @ConfiguredJsonCodec case class Broadcast(
      msgId: Int,
      message: Int
  )

  @ConfiguredJsonCodec case class BroadcastOk(
      msgId: Int
  )

  @ConfiguredJsonCodec case class Read(msgId: Int)
  @ConfiguredJsonCodec case class ReadOk(
      messages: List[Int],
      msgId: Int
  )
}
