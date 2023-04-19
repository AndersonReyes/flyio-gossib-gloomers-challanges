package com.github.andersonreyes.api

import com.github.andersonreyes.api.Body._
import com.github.andersonreyes.api.Message._
import com.github.andersonreyes.api.Counter

import java.util.UUID.randomUUID
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Node private (
    nodeId: String,
    private var neighbors: Map[String, Set[Long]]
) extends Server {

  private var nodeMsgs: Set[Long] = Set()

  override def handleMessage(msg: Message): List[Message] = if (
    // If the message is not for this node, ignore
    msg.dest != nodeId
  ) { List.empty[Message] }
  else {
    msg match {
      case EchoMessage(src, dest, body) =>
        List(
          EchoOkMessage(
            nodeId,
            src,
            EchoOk(Counter.increment, body.echo, body.msgId)
          )
        )
      // case init: InitMessage =>
      //   InitOkMessage(init.dest, init.src, InitOk(init.body.msgId))
      case GenerateMessage(src, dest, body) =>
        List(
          GenerateMessageOk(
            nodeId,
            src,
            GenerateOk(randomUUID().toString(), Counter.increment, body.msgId)
          )
        )

      case TopologyMessage(src, dest, body) => {
        neighbors = body.topology(nodeId).map((_, Set.empty[Long])).toMap
        List(TopologyOkMessage(nodeId, src, TopologyOk(body.msgId)))
      }

      // Should we put sent messages in a temp buffer and ack when we receive this?
      case BroadcastOkMessage(src, _, body) => {
        neighbors = neighbors.updated(
          src,
          neighbors.getOrElse(src, Set.empty[Long]) + body.replyTo
        )
        List.empty[Message]
      }

      case BroadcastMessage(src, _, body) => {

        nodeMsgs = nodeMsgs + body.message

        val ok = BroadcastOkMessage(
          nodeId,
          src,
          BroadcastOk(body.msgId)
        )

        // Broadcast entire state to neighbors who have not seen this message already
        // grab all the messages seen
        val broadcastMsgs = neighbors.flatMap { case (neighbor, neighborMsgs) =>
          // send only the diff. Exclude msgs node already knows about
          (nodeMsgs diff neighborMsgs).map(m =>
            BroadcastMessage(
              nodeId,
              neighbor,
              Broadcast(m, m)
            )
          )

        }.toList

        ok +: broadcastMsgs
      }

      case ReadMessage(src, _, body) =>
        List(ReadOkMessage(nodeId, src, ReadOk(nodeMsgs.toList, body.msgId)))
    }
  }

}

object Node {
  def of(nodeId: String, neighbors: List[String]): Node =
    new Node(nodeId, neighbors.map((_, Set.empty[Long])).toMap)
}
