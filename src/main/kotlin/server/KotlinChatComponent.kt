package com.lucaslpmoura.kotlin_chat.server

abstract class KotlinChatComponent(
    val id: String,
    var name: String,
    private val mediator: UserRoomMediatorInterface
) {
}