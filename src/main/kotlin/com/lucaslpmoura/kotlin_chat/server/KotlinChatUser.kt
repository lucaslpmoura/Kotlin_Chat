package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

class KotlinChatUser(id: String, val address: String, name: String, mediator: UserRoomMediatorInteface)
    : KotlinChatComponent(id, name, mediator)
{
}