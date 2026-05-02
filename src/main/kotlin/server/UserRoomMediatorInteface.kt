package com.lucaslpmoura.kotlin_chat.server

interface UserRoomMediatorInteface {
    public fun addRoom(room: KotlinChatRoom)
    public fun removeRoom(room: KotlinChatRoom)
    public fun addUser(user: KotlinChatUser)
    public fun removeUser(user: KotlinChatUser)
    public fun addUserToRoom(user: KotlinChatUser, room: KotlinChatRoom)
    public fun removeUserFromRoom(user: KotlinChatUser, room: KotlinChatRoom)
    public fun getUserRoom(user: KotlinChatUser): KotlinChatRoom?
    public fun getRoomUsers(room: KotlinChatRoom): Set<KotlinChatUser>
    public fun getNumOfUsers(): Int
    public fun getNumOfRooms(): Int
    public fun isUserConnected(user: KotlinChatUser) : Boolean
    public fun getUserById(id: String) : KotlinChatUser
    public fun getRoomById(id: String) : KotlinChatRoom
    public fun getAllRooms() : Set<KotlinChatRoom>
    public fun getAllUsers() : Set<KotlinChatUser>
}