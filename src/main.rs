use std::collections::{HashMap, HashSet};
use std::io::{self, BufRead};
use std::sync::mpsc::Sender;

use serde::{Deserialize, Serialize};
use uuid::Uuid;


struct KVStore {

}
#[derive(Serialize, Deserialize, Debug)]
#[serde(tag = "type")]
enum Response {

    #[serde(rename = "echo_ok")]
    EchoOk {
        msg_id: u64,
        in_reply_to: u64,
        echo: String,
    },

    #[serde(rename = "init_ok")]
    InitOk { in_reply_to: u64, msg_id: u64 },

    #[serde(rename = "generate_ok")]
    GenerateOk {
        id: String,
        msg_id: u64,
        in_reply_to: u64,
    },

    #[serde(rename = "topology_ok")]
    TopologyOk { msg_id: u64, in_reply_to: u64 },

    #[serde(rename = "broadcast_ok")]
    BroadcastOk { msg_id: u64, in_reply_to: u64 },

    #[serde(rename = "read_ok")]
    ReadOk {
        msg_id: u64,
        in_reply_to: u64,
        // value: i64,
        messages: Vec<u64>
    },

    #[serde(rename = "gossip_ok")]
    GossipOk { messages: Vec<u64> },

    #[serde(rename = "add_ok")]
    AddOk { msg_id: u64, in_reply_to: u64 },

    // Ignore output for messages which provide no response
    #[serde(rename = "noop")]
    Ignore,

    #[serde(rename = "gossip")]
    Gossip { messages: Vec<u64> },
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(tag = "type")]
enum Request {
    #[serde(rename = "echo")]
    Echo { msg_id: u64, echo: String },

    #[serde(rename = "init")]
    Init {
        msg_id: u64,
        node_id: String,
        node_ids: Vec<String>,
    },


    #[serde(rename = "generate")]
    Generate { msg_id: u64 },
    #[serde(rename = "topology")]
    Topology {
        msg_id: u64,
        topology: HashMap<String, Vec<String>>,
    },

    #[serde(rename = "broadcast")]
    Broadcast { msg_id: u64, message: u64 },

    #[serde(rename = "read")]
    Read { msg_id: u64 },

    #[serde(rename = "gossip")]
    Gossip { messages: Vec<u64> },
    #[serde(rename = "gossip_ok")]
    GossipInit,

    #[serde(rename = "add")]
    Add { delta: i64, msg_id: u64 },
    #[serde(rename = "shutdown")]
    Shutdown,

}

#[derive(Serialize, Deserialize, Debug)]
struct Message<T> {
    pub src: String,
    pub dest: String,
    pub body: T,
}

impl<T> Message<T> {
    fn reply(&self, body: Response) -> Message<Response> {
        Message {
            src: self.dest.clone(),
            dest: self.src.clone(),
            body,
        }
    }
}

struct Node {
    next_msg_id: u64,
    node_id: String,
    messages: HashSet<u64>,
    neighbors: HashSet<String>,
    neighbors_seen: HashMap<String, HashSet<u64>>,
}

impl Node {
    pub fn new() -> Self {
        Self {
            next_msg_id: 0,
            neighbors: HashSet::new(),
            messages: HashSet::new(),
            neighbors_seen: HashMap::new(),
            node_id: String::new(),
        }
    }

    fn gen_msg_id(&mut self) -> u64 {
        self.next_msg_id += 1;
        self.next_msg_id
    }

    fn gossip(tx: Sender<Message<Request>>) {
        std::thread::spawn(move || loop {
            std::thread::sleep(std::time::Duration::from_millis(100));
            tx.send(Message {
                src: String::default(),
                dest: String::default(),
                body: Request::GossipInit,
            })
            .unwrap();
        });
    }

    fn start_parsing(
        tx: Sender<Message<Request>>,
    ) -> std::thread::JoinHandle<Result<(), std::sync::mpsc::SendError<Message<Request>>>> {
        std::thread::spawn(move || {
            for line in io::stdin().lock().lines() {
                let line = line.expect("Failed to read line");
                let msg: Message<Request> =
                    serde_json::from_str(&line).expect(&format!("failed to parse line: {}", line));

                if let Request::Shutdown = msg.body {
                    break;
                }

                tx.send(msg).unwrap();
            }

            Ok::<(), std::sync::mpsc::SendError<Message<Request>>>(())
        })
    }

    fn handler(&mut self, msg: Message<Request>, buff: &mut Vec<Message<Response>>) {
        let next_msg_id = self.gen_msg_id();

        let mut reply = msg.reply(Response::Ignore);

        let body: Option<Response> = match msg.body {
            Request::Init {
                msg_id,
                node_id,
                node_ids,
                ..
            } => {
                self.node_id = node_id;
                // build initial neighbors from all list
                self.neighbors = node_ids
                    .into_iter()
                    .filter(|nid| nid != &self.node_id)
                    .collect();

                eprintln!("neighbors: {:#?}", self.neighbors);

                Some(Response::InitOk {
                    in_reply_to: msg_id,
                    msg_id: next_msg_id,
                })
            }

            Request::Echo { msg_id, echo } => Some(Response::EchoOk {
                msg_id: next_msg_id,
                in_reply_to: msg_id,
                echo: echo.clone(),
            }),

            Request::Generate { msg_id } => Some(Response::GenerateOk {
                id: Uuid::new_v4().to_string(),
                msg_id: next_msg_id,
                in_reply_to: msg_id,
            }),

            Request::Topology { msg_id, topology } => {
                self.neighbors = topology
                    .get(&self.node_id)
                    .expect("Node not in topology")
                    .into_iter()
                    .map(|s| s.clone())
                    .collect();

                Some(Response::TopologyOk {
                    in_reply_to: msg_id,
                    msg_id: next_msg_id,
                })
            }

            //Request::Read { msg_id } => Some(Response::ReadOk {
            //    msg_id: next_msg_id,
            //    in_reply_to: msg_id,
            //    value: -99,
            //}),

            Request::Read { msg_id } => Some(Response::ReadOk {
                msg_id: next_msg_id,
                in_reply_to: msg_id,
                messages: self.messages.clone().into_iter().collect(),
            }),

            // Request::BroadcastOk { .. } => None,

            //Request::Add { msg_id, .. } => Some(Response::AddOk {
            //    msg_id: next_msg_id,
            //    in_reply_to: msg_id,
            //}),

            Request::GossipInit => {
                for node_id in &self.neighbors {
                    let known_msgs = self
                        .neighbors_seen
                        .entry(node_id.clone())
                        .or_insert(HashSet::new());

                    let unknown_msgs: Vec<u64> = self
                        .messages
                        .symmetric_difference(&known_msgs)
                        .map(|f| f.to_owned())
                        .collect();

                    if unknown_msgs.len() != 0 {
                        buff.push(Message {
                            src: self.node_id.clone(),
                            dest: node_id.clone(),
                            body: Response::Gossip {
                                messages: unknown_msgs,
                            },
                        })
                    }
                }
                None
            }

            Request::Gossip { messages, .. } => {
                self.neighbors_seen
                    .entry(msg.src.clone())
                    .or_insert(HashSet::new())
                    .extend(messages.clone());

                self.messages.extend(&messages);

                None
            }

            Request::Broadcast { msg_id, message } => {
                self.messages.insert(message);

                Some(Response::BroadcastOk {
                    msg_id: next_msg_id,
                    in_reply_to: msg_id,
                })
            }

            _ => panic!("invalid message {:#?}", msg),
        };

        body.map(|b| {
            reply.body = b;
            buff.push(reply)
        });
    }


    // pub fn send_rpc(msg: &Message) {}
}


fn main() -> Result<(), std::sync::mpsc::SendError<Message<Request>>> {
    let (tx, rx) = std::sync::mpsc::channel();

    Node::gossip(tx.clone());
    let t = Node::start_parsing(tx);

    let mut node = Node::new();
    for m in rx {
        let mut replies: Vec<Message<Response>> = Vec::new();
        node.handler(m, &mut replies);

        for reply in replies {
            println!(
                "{}",
                serde_json::to_string(&reply).expect("Failed to serialize msg to string")
            );
        }
    }

    t.join().unwrap().unwrap();
    Ok(())
}
