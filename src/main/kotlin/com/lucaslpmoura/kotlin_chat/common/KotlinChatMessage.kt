package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.common

import com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage.Type

data class KotlinChatMessage(val origin: String, val type: Type, val data: String) {
    val timestamp = System.currentTimeMillis()

    enum class Type {
        CONNECT, DISCONNECT, AFK, LIST_ROOMS, JOIN_ROOM, LEAVE_ROOM, TEXT, ERROR
    }


}

fun parseMessageFromClient(message : KotlinChatMessage) : Map<String, String>{
    return when(message.type) {
        Type.CONNECT -> {
            if(message.data.isEmpty()){
                throw Exception("name not present.")
            }
            mapOf("name" to message.data)
        }
        else -> {
            mapOf<String, String>("content" to "")
        }
    }
}

/*
Message Types and Server Returns

All messages follow the same structure:

TYPE | DATA1 | DATA2 | DATA3 | ... | DATAn |

where TYPE is a byte representation of the names of the available message types.
DATAn is the byte representation for the data/metadata, for each type of message.

The 0x10 byte is used as a separtor for each section. He will be represented by the ' | ' char.

If a message is processed successfully, the Server will return a message with the same type.
Otherwise, the ERROR type will be sent.
Only the server can use the ERROR type.

The messages will be shown in pairs, as follows:


CONNECT

Client  -> CONNECT | USERNAME | -> Server
Server -> CONNECT | USER_ID

DISCONNECT

Client -> DISCONNECT | USER_ID |
Server -> DISCONNECT |

The server can also send a disconnect message without a Client request, as to kick them out.

 */