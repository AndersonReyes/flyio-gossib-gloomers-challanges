use serde::{Deserialize, Serialize};
use serde_json::Result;
use std::io;

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
    InitOk { in_reply_to: u64 },
}

#[derive(Serialize, Deserialize, Debug)]
struct Message {
    pub src: String,
    pub dest: String,
    pub body: Body,
}

impl Message {
    fn reply(&self) -> Message {
        let out_body = match &self.body {
            Body::Init { msg_id, .. } => Body::InitOk {
                in_reply_to: *msg_id,
            },
            Body::Echo { msg_id, echo } => Body::EchoOk {
                msg_id: *msg_id,
                in_reply_to: *msg_id,
                echo: echo.clone(),
            },
            _ => panic!("noooo!"),
        };

        Message {
            src: self.dest.clone(),
            dest: self.src.clone(),
            body: out_body,
        }
    }
}

fn main() -> Result<()> {
    loop {
        let mut raw_msg = String::new();

        io::stdin()
            .read_line(&mut raw_msg)
            .expect("failed to read from stdin");

        let msg: Message = serde_json::from_str(&raw_msg).unwrap();

        println!("{}", serde_json::to_string(&msg.reply())?);
    }

    Ok(())
}
