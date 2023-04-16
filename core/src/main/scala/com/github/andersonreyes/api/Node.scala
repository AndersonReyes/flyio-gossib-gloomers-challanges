package com.github.andersonreyes.api

import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Body._
import scala.collection.mutable.ListBuffer

import java.util.UUID.randomUUID

class Node extends Server {

  private var topology: Option[Topology] = None
  private var messages: ListBuffer[Int] = ListBuffer.empty[Int]

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

    case TopologyMessage(src, dest, body) => {
      topology = Some(Topology(body.topology, body.msgId))
      TopologyOkMessage(dest, src, TopologyOk(body.msgId))
    }

    case BroadcastMessage(src, dest, body) => {
      messages += body.message
      BroadcastOkMessage(dest, src, BroadcastOk(body.msgId))
    }

    case ReadMessage(src, dest, body) =>
      ReadOkMessage(dest, src, ReadOk(messages.toList, body.msgId))
  }

}
