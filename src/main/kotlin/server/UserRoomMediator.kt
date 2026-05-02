package com.lucaslpmoura.kotlin_chat.server

import kotlin.collections.get


class UserRoomMediator : UserRoomMediatorInterface {
    private val userRoomMap: MutableMap<KotlinChatUser, KotlinChatRoom?> = mutableMapOf<KotlinChatUser, KotlinChatRoom?>()
    private val roomUserMap: MutableMap<KotlinChatRoom, MutableSet<KotlinChatUser>> = mutableMapOf<KotlinChatRoom, MutableSet<KotlinChatUser>>()


    override fun addRoom(room: KotlinChatRoom) {
        if (roomUserMap.containsKey(room)){
            throw Exception("Room already exists.")
        }
        roomUserMap[room] = mutableSetOf<KotlinChatUser>()
    }

    override fun removeRoom(room: KotlinChatRoom) {
        checkIfRoomExists(room)

        if(roomUserMap[room]!!.isNotEmpty()){
            for(user in roomUserMap[room]!!){
                userRoomMap[user] = null
            }
        }
        roomUserMap.remove(room)
    }

    override fun removeAllRooms() {
        for(user in userRoomMap.keys){
            userRoomMap[user] = null
        }
        roomUserMap.clear()

    }

    override fun addUser(user: KotlinChatUser) {
        if(userRoomMap.containsKey(user)){
            throw Exception("User already on server.")
        }
        userRoomMap[user] = null
    }

    override fun removeUser(user: KotlinChatUser) {
        checkIfUserExists(user)

        if(userRoomMap[user] != null){
            roomUserMap[userRoomMap[user]]?.remove(user)
        }
        userRoomMap.remove(user)
    }

    override fun removeAllUsers() {
        for(room in roomUserMap.keys){
            roomUserMap[room]?.clear()
        }
        userRoomMap.clear()
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

    override fun getUserRoom(user: KotlinChatUser): KotlinChatRoom? {
        checkIfUserExists(user)
        return userRoomMap[user]
    }

    override fun getRoomUsers(room: KotlinChatRoom): Set<KotlinChatUser> {
        checkIfRoomExists(room)
        return roomUserMap[room]!!.toSet()
    }

    override fun getNumOfUsers(): Int {
        return userRoomMap.size
    }

    override fun getNumOfRooms(): Int {
        return roomUserMap.size
    }

    override fun isUserConnected(user: KotlinChatUser) : Boolean{
        return userRoomMap.containsKey(user)
    }

    override fun getUserById(id: String): KotlinChatUser {
        for (user in userRoomMap.keys){
            if(user.id == id){
                return user
            }
        }
        throw Exception("No user with id $id exists.")
    }

    override fun getRoomById(id: String): KotlinChatRoom {
        for (room in roomUserMap.keys){
            if(room.id == id){
                return room
            }
        }
        throw Exception("No room with id $id exists.")
    }

    override fun getAllRooms(): Set<KotlinChatRoom> {
        return roomUserMap.keys.toSet()
    }

    override fun getAllUsers(): Set<KotlinChatUser> {
        return userRoomMap.keys.toSet()
    }


    private fun checkIfRoomExists(room: KotlinChatRoom) {
        if(!roomUserMap.containsKey(room)){
            throw Exception("Room ${room.id} does not exist.")
        }
    }

    private fun checkIfUserExists(user: KotlinChatUser) {
        if(!userRoomMap.containsKey(user)){
            throw Exception("User ${user.id} is not on server.")
        }

    }
    private fun checkIfUserAndRoomExists(user: KotlinChatUser, room: KotlinChatRoom) {
        checkIfRoomExists(room)
        checkIfUserExists(user)
    }

}

