package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

import kotlin.to

class UserRoomMediator : UserRoomMediatorInteface {
    override val userRoomMap: MutableMap<KotlinChatUser, KotlinChatRoom?> = mutableMapOf<KotlinChatUser, KotlinChatRoom?>()
    override val roomUserMap: MutableMap<KotlinChatRoom, MutableSet<KotlinChatUser>> = mutableMapOf<KotlinChatRoom, MutableSet<KotlinChatUser>>()

    override fun addUserToRoom(
        user: KotlinChatUser,
        room: KotlinChatRoom
    ) {
        if(userRoomMap[user] != null) {
            throw Exception("User ${user.id} already in room ${room.id}")
        }
        if(room.numOfUsers >= room.MAX_USERS){
            throw Exception("Maximum number of users reached for room ${room.id}")
        }

    }

    override fun removeUserFromRoom(
        user: KotlinChatUser,
        room: KotlinChatRoom
    ) {
        TODO("Not yet implemented")
    }
}

