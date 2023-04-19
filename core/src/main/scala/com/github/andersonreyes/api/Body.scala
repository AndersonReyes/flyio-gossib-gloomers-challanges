package com.github.andersonreyes.api

import io.circe.generic.extras._

object Body {
  import com.github.andersonreyes.api.Config._

  @ConfiguredJsonCodec case class Echo(
      msgId: Long,
      echo: String
  )
  @ConfiguredJsonCodec case class EchoOk(
      msgId: Long,
      echo: String,
      @JsonKey("in_reply_to") replyTo: Long
  )

  @ConfiguredJsonCodec case class Init(
      msgId: Long,
      nodeId: String,
      nodeIds: List[String]
  )
  @ConfiguredJsonCodec case class InitOk(
      @JsonKey("in_reply_to") replyTo: Long
  )

  @ConfiguredJsonCodec case class ErrorBody(
      @JsonKey("in_reply_to") replyTo: Long,
      code: Long,
      text: String
  )

  @ConfiguredJsonCodec case class Generate(
      msgId: Long
  )
  @ConfiguredJsonCodec case class Topology(
      topology: Map[String, List[String]],
      msgId: Long
  )

  @ConfiguredJsonCodec case class TopologyOk(
      @JsonKey("in_reply_to") replyTo: Long
  )
  @ConfiguredJsonCodec case class GenerateOk(
      id: String,
      msgId: Long,
      @JsonKey("in_reply_to") replyTo: Long
  )

  @ConfiguredJsonCodec case class Broadcast(
      msgId: Long,
      message: Long
  )

  @ConfiguredJsonCodec case class BroadcastOk(
      @JsonKey("in_reply_to") replyTo: Long
  )

  @ConfiguredJsonCodec case class Read(msgId: Long)
  @ConfiguredJsonCodec case class ReadOk(
      messages: List[Long],
      @JsonKey("in_reply_to") replyTo: Long
  )
}
