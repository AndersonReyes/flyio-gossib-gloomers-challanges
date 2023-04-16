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
      @JsonKey("in_reply_to") replyTo: Int,
      `type`: String = "echo_ok"
  )

  @ConfiguredJsonCodec case class Init(
      msgId: Int,
      nodeId: String,
      nodeIds: List[String]
  )
  @ConfiguredJsonCodec case class InitOk(
      @JsonKey("in_reply_to") replyTo: Int,
      `type`: String = "init_ok"
  )

  @ConfiguredJsonCodec case class ErrorBody(
      @JsonKey("in_reply_to") replyTo: Int,
      code: Int,
      text: String,
      `type`: String = "error"
  )

  @ConfiguredJsonCodec case class Generate(
      msgId: Int
  )
  @ConfiguredJsonCodec case class Topology(
      topology: Map[String, List[String]],
      msgId: Int
  )

  @ConfiguredJsonCodec case class TopologyOk(
      msgId: Int,
      `type`: String = "topology_ok"
  )
  @ConfiguredJsonCodec case class GenerateOk(
      id: String,
      msgId: Int,
      @JsonKey("in_reply_to") replyTo: Int,
      `type`: String = "generate_ok"
  )

  @ConfiguredJsonCodec case class Broadcast(
      msgId: Int,
      message: Int
  )

  @ConfiguredJsonCodec case class BroadcastOk(
      msgId: Int,
      `type`: String = "broadcast_ok"
  )

  @ConfiguredJsonCodec case class Read(msgId: Int)
  @ConfiguredJsonCodec case class ReadOk(
      messages: List[Int],
      msgId: Int,
      `type`: String = "read_ok"
  )
}
