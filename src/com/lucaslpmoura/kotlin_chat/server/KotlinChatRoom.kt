package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

class KotlinChatRoom(
    val name: String,
    val id: String,
    private val mediator: UserRoomMediatorInteface
) {

    val MAX_USERS = 3
    var numOfUsers = 0
}