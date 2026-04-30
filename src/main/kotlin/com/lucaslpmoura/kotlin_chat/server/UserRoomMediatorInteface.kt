package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

interface UserRoomMediatorInteface {
    public fun addRoom(room: KotlinChatRoom)
    public fun removeRoom(room: KotlinChatRoom)
    public fun addUser(user: KotlinChatUser)
    public fun removeUser(user: KotlinChatUser)
    public fun addUserToRoom(user: KotlinChatUser, room: KotlinChatRoom)
    public fun removeUserFromRoom(user: KotlinChatUser, room: KotlinChatRoom)
}