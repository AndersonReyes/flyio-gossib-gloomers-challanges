package com.github.andersonreyes.api

import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Body._

import java.util.UUID.randomUUID

class Node extends Server {

  override def handleMessage(msg: Message): Message = msg match {
    case EchoMessage(src, dest, body) =>
      EchoOkMessage(dest, src, EchoOk(body.msgId, body.echo, body.msgId))
    case init: InitMessage =>
      InitOkMessage(init.dest, init.src, InitOk(init.body.msgId))
    case GenerateMessage(src, dest, body) =>
      GenerateMessageOk(
        dest,
        src,
        GenerateOk(randomUUID().toString(), body.msgId, body.msgId)
      )
  }
}
