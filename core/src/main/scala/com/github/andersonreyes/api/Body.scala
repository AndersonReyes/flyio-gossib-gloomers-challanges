package com.github.andersonreyes.api

import io.circe.generic.extras._

object Body {
  import com.github.andersonreyes.api.Config._

  @ConfiguredJsonCodec case class Echo(
      msgId: Int,
      echo: String,
      `type`: String = "echo"
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
      nodeIds: List[String],
      `type`: String = "init"
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
      msgId: Int,
      `type`: String = "generate"
  ) {
    // So that when messages get decoded over network with invalid type,
    // this throws an error
    require(`type` == "generate")
  }

  @ConfiguredJsonCodec case class GenerateOk(
      id: String,
      msgId: Int,
      @JsonKey("in_reply_to") replyTo: Int,
      `type`: String = "generate_ok"
  )
}
