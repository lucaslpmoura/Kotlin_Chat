package com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server

import kotlin.to

class UserRoomMediator : UserRoomMediatorInteface {
    private val userRoomMap: MutableMap<KotlinChatUser, KotlinChatRoom?> = mutableMapOf<KotlinChatUser, KotlinChatRoom?>()
    private val roomUserMap: MutableMap<KotlinChatRoom, MutableSet<KotlinChatUser>> = mutableMapOf<KotlinChatRoom, MutableSet<KotlinChatUser>>()


    override fun addRoom(room: KotlinChatRoom) {
        if (roomUserMap.containsKey(room)){
            throw Exception("Room already exists.")
        }
        roomUserMap[room] = mutableSetOf<KotlinChatUser>()
    }

    override fun removeRoom(room: KotlinChatRoom) {
        if(!roomUserMap.containsKey(room)){
            throw Exception("Room does not exist.")
        }
        roomUserMap.remove(room)
    }

    override fun addUser(user: KotlinChatUser) {
        if(userRoomMap.containsKey(user)){
            throw Exception("User already on server.")
        }
        userRoomMap[user] = null
    }

    override fun removeUser(user: KotlinChatUser) {
        if(!userRoomMap.containsKey(user)){
            throw Exception("User is not on server.")
        }
        userRoomMap.remove(user)
    }


    override fun addUserToRoom(user: KotlinChatUser, room: KotlinChatRoom) {
        checkIfUserAndRoomExists(user, room)
        if(room.numOfUsers >= room.MAX_USERS){
            throw Exception("Maximum number of users reached for room ${room.id}.")
        }
        if(userRoomMap[user] != null) {
            throw Exception("User ${user.id} already in room ${room.id}.")
        }

        userRoomMap[user] = room
        roomUserMap[room]!!.add(user)
        room.numOfUsers++
    }
    override fun removeUserFromRoom(user: KotlinChatUser, room: KotlinChatRoom) {
        checkIfUserAndRoomExists(user, room)
        if(room.numOfUsers <= 0){
            throw Exception("Room ${room.id} already has no users.")
        }
        if(userRoomMap[user] != room) {
            throw Exception("User ${user.id} is not at room ${room.id}.")
        }

        userRoomMap[user] = null
        roomUserMap[room]!!.remove(user)
        room.numOfUsers--
    }

    private fun checkIfUserAndRoomExists(user: KotlinChatUser, room: KotlinChatRoom) {
        if(!roomUserMap.containsKey(room)){
            throw Exception("Room ${room.id} does not exist.")
        }
        if(!userRoomMap.containsKey(user)){
            throw Exception("User ${user.id} is not on server.")
        }
    }
}

