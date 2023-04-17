package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import com.github.andersonreyes.api.Message._

import java.util.UUID.randomUUID
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Node(nodeId: String, private var neighbors: List[String] = List())
    extends Server {

  private var messages: mutable.Set[Int] = mutable.Set.empty[Int]

  override def handleMessage(msg: Message): Option[Message] = if (
    // If the message is not for this node return none
    msg.dest != nodeId
  ) { None }
  else {
    val response = msg match {
      case EchoMessage(src, dest, body) =>
        EchoOkMessage(nodeId, src, EchoOk(body.msgId, body.echo, body.msgId))
      // case init: InitMessage =>
      //   InitOkMessage(init.dest, init.src, InitOk(init.body.msgId))
      case GenerateMessage(src, dest, body) =>
        GenerateMessageOk(
          nodeId,
          src,
          GenerateOk(randomUUID().toString(), body.msgId, body.msgId)
        )

      case TopologyMessage(src, dest, body) => {
        neighbors = body.topology(nodeId)
        TopologyOkMessage(nodeId, src, TopologyOk(body.msgId))
      }

      case BroadcastMessage(src, dest, body) => {
        messages += body.message
        BroadcastOkMessage(nodeId, src, BroadcastOk(body.msgId))
      }

      case ReadMessage(src, dest, body) =>
        ReadOkMessage(nodeId, src, ReadOk(messages.toList, body.msgId))
    }

    Some(response)
  }

}
