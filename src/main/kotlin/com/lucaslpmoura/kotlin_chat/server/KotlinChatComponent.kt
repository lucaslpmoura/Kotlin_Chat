package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

abstract class KotlinChatComponent(
    val id: String,
    val name: String,
    private val mediator: UserRoomMediatorInteface
) {
}