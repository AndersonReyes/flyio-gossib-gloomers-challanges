use serde::{Deserialize, Serialize};
use serde_json::Result;
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
}

#[derive(Serialize, Deserialize, Debug)]
struct Message {
    pub src: String,
    pub dest: String,
    pub body: Body,
}

impl Message {
    fn reply(&self, next_msg_id: u64) -> Message {
        let out_body = match &self.body {
            Body::Init { msg_id, .. } => Body::InitOk {
                in_reply_to: *msg_id,
                msg_id: next_msg_id,
            },
            Body::Echo { msg_id, echo } => Body::EchoOk {
                msg_id: next_msg_id,
                in_reply_to: *msg_id,
                echo: echo.clone(),
            },
            Body::Generate { msg_id } => Body::GenerateOk {
                id: Uuid::new_v4().to_string(),
                msg_id: next_msg_id,
                in_reply_to: *msg_id,
            },

            _ => panic!("invalid message {:#?}", self),
        };

        Message {
            src: self.dest.clone(),
            dest: self.src.clone(),
            body: out_body,
        }
    }
}

struct Node {
    counter: u64,
}

impl Node {
    pub fn new() -> Self {
        Self { counter: 0 }
    }

    fn gen_msg_id(&mut self) -> u64 {
        self.counter += 1;
        self.counter
    }
    pub fn run(&mut self) {
        loop {
            let mut raw_msg = String::new();

            io::stdin()
                .read_line(&mut raw_msg)
                .expect("failed to read from stdin");

            let msg: Message = serde_json::from_str(&raw_msg).unwrap();

            println!(
                "{}",
                serde_json::to_string(&msg.reply(self.gen_msg_id()))
                    .expect("Failed to serialize msg to string")
            );
        }
    }
}

fn main() -> Result<()> {
    let mut node = Node::new();
    node.run();
    Ok(())
}
