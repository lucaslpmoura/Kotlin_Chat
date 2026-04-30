package com.lucaslpmoura.kotlin_chat.server

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.parseMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.parseMessageFromClient
import java.net.ServerSocket
import java.net.Socket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KotlinChatServer {

    val port : Int = 7960
    val socket : ServerSocket = ServerSocket()

    val USER_NAME_BUFFER_SIZE : Int = 32
    val ROOM_NAME_BUFFER_SIZE : Int = 64

    val MAX_ROOMS : Int = 3
    // Later use -> var numOfRooms: Int = 0
    val MAX_USERS : Int = 3
    var numOfUsers: Int = 0

    val mediator: UserRoomMediatorInteface = UserRoomMediator()

    private fun estabilishConnection() {
        try{
            val clientSocket : Socket = socket.accept()
            val newUser = KotlinChatUser(
                id = generateUUID(),
                name = "CONNECTING_USER",
                socket = clientSocket,
                mediator = mediator,
            )
            try{
                newUser.name = readUserName(newUser)
                addUser(newUser)
                sendMessage(KotlinChatMessage.Type.CONNECT, newUser)
            }catch (e: Exception){
                println("Failed to add user: ${e.message}")
                sendMessage(KotlinChatMessage.Type.ERROR, newUser, "Could not connect to server.")
            }

        }catch(e: Exception) {
            println("Failed to connect user: ${e.message}")
        }
    }

    public fun processMessage(message: KotlinChatMessage) {
        when (message.type) {
            KotlinChatMessage.Type.DISCONNECT -> disconnectClient(message)
            KotlinChatMessage.Type.AFK -> TODO()
            KotlinChatMessage.Type.LIST_ROOMS -> TODO()
            KotlinChatMessage.Type.JOIN_ROOM -> TODO()
            KotlinChatMessage.Type.LEAVE_ROOM -> TODO()
            KotlinChatMessage.Type.TEXT -> TODO()
            KotlinChatMessage.Type.ERROR -> TODO()
            else -> {
                println("Failed to process message of type ${message.type}")
                return
            }
        }
    }

    private fun disconnectClient(message: KotlinChatMessage) {
        try{
            val userId = parseMessageFromClient(message)["id"]!!
            val user = mediator.getUserById(userId)
            if(message.origin == "SERVER" || message.origin == user.address){
                removeUser(user)
            }else{
                throw Exception("You don't have permission do disconnect users.")
            }
        }catch(e: Exception){
            println("Failed to disconnect user: ${e.message}")

        }
    }

    private fun addUser(user: KotlinChatUser) {
        if(mediator.getNumOfUsers() < MAX_USERS) {
            mediator.addUser(user)
        }else{
            throw Exception("Cannot add user ${user.id} -- server is full.")
        }
    }

    private fun removeUser(user: KotlinChatUser) {
        if(mediator.isUserConnected(user)){
            mediator.removeUser(user)
        }else{
            throw Exception("Cannot remove user ${user.id} -- user is not on server.")
        }
    }

    private fun sendMessage(type: KotlinChatMessage.Type, user: KotlinChatUser, data: String = "") {

    }

    private fun readUserName(user: KotlinChatUser): String {
        val buffer = ByteArray(USER_NAME_BUFFER_SIZE)

        val bytesRead = user.input?.read(buffer) ?: return ""

        val messageFromBytes = parseMessageFromBytes(user.address, buffer, bytesRead)

        try{
            return parseMessageFromClient(messageFromBytes)["name"] ?: throw Exception("Could not read user's name!")
        }catch(e: Exception){
            throw Exception("Failed to read user name: ${e.message}")
        }

    }


    @OptIn(ExperimentalUuidApi::class)
    private fun generateUUID() : String{
        return Uuid.random().toString()
    }
}