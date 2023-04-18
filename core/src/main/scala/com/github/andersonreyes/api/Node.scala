package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import com.github.andersonreyes.api.Message._

import java.util.UUID.randomUUID
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Node private (
    nodeId: String,
    private var seenMessages: Map[String, Set[Int]]
) extends Server {

  override def handleMessage(msg: Message): List[Message] = if (
    // If the message is not for this node, ignore
    msg.dest != nodeId
  ) { List.empty[Message] }
  else {
    msg match {
      case EchoMessage(src, dest, body) =>
        List(
          EchoOkMessage(nodeId, src, EchoOk(body.msgId, body.echo, body.msgId))
        )
      // case init: InitMessage =>
      //   InitOkMessage(init.dest, init.src, InitOk(init.body.msgId))
      case GenerateMessage(src, dest, body) =>
        List(
          GenerateMessageOk(
            nodeId,
            src,
            GenerateOk(randomUUID().toString(), body.msgId, body.msgId)
          )
        )

      case TopologyMessage(src, dest, body) => {
        seenMessages = body.topology(nodeId).map((_, Set.empty[Int])).toMap
        List(TopologyOkMessage(nodeId, src, TopologyOk(body.msgId)))
      }

      // Should we put sent messages in a temp buffer and ack when we receive this?
      case BroadcastOkMessage(src, dest, body) => List.empty[Message]

      case BroadcastMessage(src, dest, body) => {
        // add msg to seen map
        seenMessages = seenMessages.updated(
          src,
          seenMessages.getOrElse(src, Set.empty[Int]) + body.message
        )

        // Broadcast to neighbors

        val out = BroadcastOkMessage(
          nodeId,
          src,
          BroadcastOk(body.msgId)
        )

        // Broadcast to neighbors who have not seen this message already
        out +: seenMessages
          .filterNot(_._2.contains(body.msgId))
          .map { case (neighborId, msgs) =>
            BroadcastMessage(
              nodeId,
              neighborId,
              Broadcast(body.msgId, body.message)
            )
          }
          .toList
      }

      case ReadMessage(src, dest, body) =>
        List(
          ReadOkMessage(
            nodeId,
            src,
            ReadOk(seenMessages(nodeId).toList, body.msgId)
          )
        )
    }
  }

}

object Node {
  def of(nodeId: String, neighbors: List[String]): Node =
    new Node(nodeId, neighbors.map((_, Set.empty[Int])).toMap)
}
