use serde::{Deserialize, Serialize};
use serde_json::Result;
use std::collections::{HashMap, HashSet};
use std::io;
use uuid::Uuid;

#[derive(Serialize, Deserialize, Debug)]
#[serde(tag = "type")]
enum Body {
    #[serde(rename = "echo")]
    Echo { msg_id: u64, echo: String },

    #[serde(rename = "echo_ok")]
    EchoOk {
        msg_id: u64,
        in_reply_to: u64,
        echo: String,
    },

    #[serde(rename = "init")]
    Init {
        msg_id: u64,
        node_id: String,
        node_ids: Vec<String>,
    },

    #[serde(rename = "init_ok")]
    InitOk { in_reply_to: u64, msg_id: u64 },

    #[serde(rename = "generate")]
    Generate { msg_id: u64 },
    #[serde(rename = "generate_ok")]
    GenerateOk {
        id: String,
        msg_id: u64,
        in_reply_to: u64,
    },

    #[serde(rename = "topology")]
    Topology {
        msg_id: u64,
        topology: HashMap<String, Vec<String>>,
    },
    #[serde(rename = "topology_ok")]
    TopologyOk { msg_id: u64, in_reply_to: u64 },

    #[serde(rename = "broadcast")]
    Broadcast { msg_id: u64, message: u64 },
    #[serde(rename = "broadcast_ok")]
    BroadcastOk { msg_id: u64, in_reply_to: u64 },

    #[serde(rename = "read")]
    Read { msg_id: u64 },
    #[serde(rename = "read_ok")]
    ReadOk {
        msg_id: u64,
        in_reply_to: u64,
        messages: Vec<u64>,
    },

    #[serde(rename = "gossip")]
    Gossip { msg_id: u64, messages: Vec<u64> },
    #[serde(rename = "gossip_ok")]
    GossipOk { msg_id: u64, messages: Vec<u64> },
    #[serde(rename = "shutdown")]
    Shutdown,

    #[serde(rename = "noop")]
    NoOp,
}

#[derive(Serialize, Deserialize, Debug)]
struct Message {
    pub src: String,
    pub dest: String,
    pub body: Body,
}

struct Node {
    counter: u64,
    topology: HashMap<String, Vec<String>>,
    node_id: String,
    messages: HashSet<u64>,
    neighbors_seen: HashMap<String, HashSet<u64>>,
}

impl Node {
    pub fn new() -> Self {
        Self {
            counter: 0,
            topology: HashMap::new(),
            messages: HashSet::new(),
            neighbors_seen: HashMap::new(),
            node_id: String::new(),
        }
    }

    fn gen_msg_id(&mut self) -> u64 {
        self.counter += 1;
        self.counter
    }

    fn handler(&mut self, msg: Message) -> Vec<Message> {
        let next_msg_id = self.gen_msg_id();

        // allows us to inject other messages in the output as well.
        // See Broadcast handler for example.
        let mut outputs: Vec<Message> = vec![];

        let out_body: Body = match msg.body {
            Body::Init {
                msg_id, node_id, ..
            } => {
                self.node_id = node_id;

                Body::InitOk {
                    in_reply_to: msg_id,
                    msg_id: next_msg_id,
                }
            }
            Body::Echo { msg_id, echo } => Body::EchoOk {
                msg_id: next_msg_id,
                in_reply_to: msg_id,
                echo: echo.clone(),
            },
            Body::Generate { msg_id } => Body::GenerateOk {
                id: Uuid::new_v4().to_string(),
                msg_id: next_msg_id,
                in_reply_to: msg_id,
            },

            Body::Topology { msg_id, topology } => {
                self.topology.clear();
                self.topology = topology.clone();
                Body::TopologyOk {
                    in_reply_to: msg_id,
                    msg_id: next_msg_id,
                }
            }

            Body::Read { msg_id } => Body::ReadOk {
                msg_id: next_msg_id,
                in_reply_to: msg_id,
                messages: self.messages.clone().into_iter().collect(),
            },

            Body::BroadcastOk { .. } => Body::NoOp,
            Body::GossipOk { messages, .. } => {
                // Ack the messages have been seen by node by storing the reply
                self.neighbors_seen
                    .entry(msg.src.clone())
                    .or_insert(HashSet::new())
                    .extend(messages);
                Body::NoOp
            }
            Body::Gossip { messages, .. } => {
                self.messages.extend(&messages);
                // Also add the messages to be seen by src node
                self.neighbors_seen
                    .entry(msg.src.clone())
                    .or_insert(HashSet::new())
                    .extend(messages.clone());

                // Add this nodes messages to the reply so we can tell src node
                // what this node has seen in same gossip interaction.
                // This allows us to better gossip and eventually propagate all messages even
                // during network partitions provided there is at least one path to another node.
                let mut reply_msgs = messages.clone();
                reply_msgs.extend(&self.messages);

                Body::GossipOk {
                    msg_id: next_msg_id,
                    messages: reply_msgs,
                }
            }

            Body::Broadcast { msg_id, message } => {
                self.messages.insert(message);

                // for each neighbor initiate gossip with the updated messsages vector
                // // TODO: to reduce the number of messages per broadcast can we pick 3 nodes at
                // random? instead of sending to all.
                let neighbors: Vec<String> = self.topology.keys().map(|k| k.clone()).collect();

                for node_id in neighbors {
                    let known_msgs = self
                        .neighbors_seen
                        .entry(node_id.clone())
                        .or_insert(HashSet::new());

                    let unknown_msgs: Vec<u64> = self
                        .messages
                        .symmetric_difference(&known_msgs)
                        .map(|f| f.to_owned())
                        .collect();
                    let mid = self.gen_msg_id();

                    outputs.push(Message {
                        src: self.node_id.clone(),
                        dest: node_id.clone(),
                        body: Body::Gossip {
                            msg_id: mid,
                            messages: unknown_msgs,
                        },
                    })
                }

                Body::BroadcastOk {
                    msg_id: next_msg_id,
                    in_reply_to: msg_id,
                }
            }

            Body::Shutdown => Body::Shutdown,

            _ => panic!("invalid message {:#?}", msg),
        };

        outputs.push(Message {
            src: msg.dest.clone(),
            dest: msg.src.clone(),
            body: out_body,
        });

        outputs
    }
    pub fn run(&mut self) {
        'outer: loop {
            let mut raw_msg = String::new();

            io::stdin()
                .read_line(&mut raw_msg)
                .expect("failed to read from stdin");

            let msg: Message = serde_json::from_str(&raw_msg).unwrap();

            let outputs: Vec<Message> = self.handler(msg);

            for m in outputs {
                if let Body::Shutdown = m.body {
                    break 'outer;
                }

                if let Body::NoOp = m.body {
                    continue;
                }
                println!(
                    "{}",
                    serde_json::to_string(&m).expect("Failed to serialize msg to string")
                );
            }
        }
    }
}

fn main() -> Result<()> {
    let mut node = Node::new();
    node.run();
    Ok(())
}
