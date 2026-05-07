package com.lucaslpmoura.kotlin_chat.common

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage.Type

data class KotlinChatMessage(val origin: String, val type: Type, val data: String) {
    val timestamp = System.currentTimeMillis()

    enum class Type {
        CONNECT, DISCONNECT, LIST_ROOMS, JOIN_ROOM, LEAVE_ROOM, TEXT, ERROR
    }


}

fun getMessageFromBytes(origin : String, bytes : ByteArray) : KotlinChatMessage{
    lateinit var type : Type
    lateinit var data : String

    var i = 0
    var byte = bytes[i]
    var outArray : MutableList<Byte> = mutableListOf<Byte>()

    // Reads until the first '|' to get the message type
    while(byte != '|'.code.toByte()){
        outArray.add(byte)
        i++
        byte = bytes[i]
    }


    val typeName = outArray.toByteArray().toString(Charsets.UTF_8)

    type = resolveTypeFromString(typeName) ?: throw Exception("Invalid message type.")
    outArray.clear()


    // The rest of the array, starting from index where the type ends plus one, is the message data.

    i++ // Skips the first '|'
    byte = bytes[i]
    while(byte != 0.toByte()){
        outArray.add(byte)
        i++
        byte = bytes[i]
    }
    data = outArray.toByteArray().toString(Charsets.UTF_8)

    return KotlinChatMessage(origin, type, data)
}

fun KotlinChatMessage.toByteArray() : ByteArray{
    val type = this.type.name
    val data = this.data
    return "$type|$data".toByteArray()
}



fun resolveTypeFromString(string : String) : KotlinChatMessage.Type?{
    return when(string) {
        "CONNECT" -> Type.CONNECT
        "DISCONNECT" -> Type.DISCONNECT
        "LIST_ROOMS" -> Type.LIST_ROOMS
        "JOIN_ROOM" -> Type.JOIN_ROOM
        "LEAVE_ROOM" -> Type.LEAVE_ROOM
        "TEXT" -> Type.TEXT
        "ERROR" -> Type.ERROR
        else -> null
    }
}

/*
Message Types and Server Returns

All messages follow the same structure:

TYPE | DATA1 | DATA2 | DATA3 | ... | DATAn |

where TYPE is a byte representation of the names of the available message types.
DATAn is the byte representation for the data/metadata, for each type of message.

The '|' byte is used as a separtor for each section.

If a message is processed successfully, the Server will return a message with the same type.
Otherwise, the ERROR type will be sent.
Only the server can use the ERROR type.


The messages will be shown in pairs, as follows:


CONNECT

Client -> TCP_HANDSHAKE
Server -> TCP_HANDSHAKE
Client  -> CONNECT | USERNAME
Server -> CONNECT | USER_ID

DISCONNECT

Client -> DISCONNECT | USER_ID
Server -> DISCONNECT

The server can also send a disconnect message without a Client request, as to kick them out.

LIST_ROOMS
Client -> LIST_ROOMS | USER_ID
Server -> LIST_ROOMS | ROOM_ID1 | ROOM_NAME1 | ROOM_ID1 | ROOM_NAME2 | ... | ROOM_IDn | ROOM_NAMEn

JOIN_ROOM
Client -> JOIN_ROOM | USER_ID | ROOM_ID
Server -> JOIN_ROOM | ROOM_ID | ROOM_NAME

LEAVE_ROOM
Client -> LEAVE_ROOM | USER_ID | ROOM_ID
Server -> LEAVE_ROOM | ROOM_ID

TEXT
Client -> TEXT | USER_ID | ROOM_ID | CONTENT
Server -> TEXT | NAME | ROOM_ID | CONTENT

 */