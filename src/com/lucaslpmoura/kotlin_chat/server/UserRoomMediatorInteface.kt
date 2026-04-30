package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

interface UserRoomMediatorInteface {
    val userRoomMap : MutableMap<KotlinChatUser, KotlinChatRoom?>
    val roomUserMap : MutableMap<KotlinChatRoom, MutableSet<KotlinChatUser>>

    public fun addUserToRoom(user: KotlinChatUser, room: KotlinChatRoom)
    public fun removeUserFromRoom(user: KotlinChatUser, room: KotlinChatRoom)
}