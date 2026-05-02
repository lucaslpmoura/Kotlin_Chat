package com.lucaslpmoura.kotlin_chat.server

class KotlinChatRoom(id: String, name: String, mediator: UserRoomMediatorInterface, val MAX_USERS : Int)
    : KotlinChatComponent(id, name, mediator ){
    public var numOfUsers : Int = 0
}