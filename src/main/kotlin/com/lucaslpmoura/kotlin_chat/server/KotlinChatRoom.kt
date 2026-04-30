package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

class KotlinChatRoom(id: String, name: String, mediator: UserRoomMediatorInteface, val MAX_USERS : Int)
    : KotlinChatComponent(id, name, mediator ){
    public var numOfUsers : Int = 0
}