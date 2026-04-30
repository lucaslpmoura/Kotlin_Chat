package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.common

data class KotlinChatMessage(val origin: String, val type: Type, val data: String) {
    val timestamp = System.currentTimeMillis()

    enum class Type {
        CONNECT, DISCONNECT, AFK, LIST_ROOMS, JOIN_ROOM, LEAVE_ROOM, TEXT, ERROR
    }
}