package com.github.andersonreyes.api

import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Body._

class Node extends Server {

  override def handleMessage(msg: Message): Message = msg match {
    case EchoMessage(src, dest, body) =>
      EchoOkMessage(dest, src, EchoOk(body.msgId, body.echo, body.msgId))
    case init: InitMessage =>
      InitOkMessage(init.dest, init.src, InitOk(init.body.msgId))
  }
}
