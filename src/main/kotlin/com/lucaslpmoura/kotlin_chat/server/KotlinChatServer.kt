package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

import com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage

class KotlinChatServer {

    val MAX_ROOMS = 3
    val MAX_USERS = 3

    val users = mutableListOf<KotlinChatUser>()
    val rooms = mutableListOf<KotlinChatRoom>()
    val mediator: UserRoomMediatorInteface = UserRoomMediator()

    private fun processMessage(message: KotlinChatMessage) {
        when (message.type) {
            KotlinChatMessage.Type.CONNECT -> connectClient(message)
            KotlinChatMessage.Type.DISCONNECT -> TODO()
            KotlinChatMessage.Type.AFK -> TODO()
            KotlinChatMessage.Type.LIST_ROOMS -> TODO()
            KotlinChatMessage.Type.JOIN_ROOM -> TODO()
            KotlinChatMessage.Type.LEAVE_ROOM -> TODO()
            KotlinChatMessage.Type.TEXT -> TODO()
            KotlinChatMessage.Type.ERROR -> TODO()
            else -> {
                println("Failed to process message of type ${message.type}")
                return
            }
        }
    }

    private fun connectClient(message: KotlinChatMessage) {

    }

    private fun disconnectClient(message: KotlinChatMessage) {

    }


}