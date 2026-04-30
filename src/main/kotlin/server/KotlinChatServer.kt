package com.lucaslpmoura.kotlin_chat.server

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.parseMessageFromClient
import java.net.ServerSocket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KotlinChatServer {

    val port : Int = 7960
    val socket : ServerSocket = ServerSocket()

    val MAX_ROOMS : Int = 3
    // Later use -> var numOfRooms: Int = 0
    val MAX_USERS : Int = 3
    var numOfUsers: Int = 0

    val mediator: UserRoomMediatorInteface = UserRoomMediator()


    private fun processMessage(message: KotlinChatMessage) {
        when (message.type) {
            KotlinChatMessage.Type.CONNECT -> connectUser(message)
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


    private fun connectUser(message: KotlinChatMessage) {
        try{
            val newUser: KotlinChatUser = KotlinChatUser(
                generateUUID(),
                message.origin,
                parseMessageFromClient(message)["username"]!!,
                mediator,

                )
            try{
                addUser(newUser)
                sendMessage(KotlinChatMessage.Type.CONNECT, newUser)
            }catch(e: Exception){
                println("Failed to connect user: ${e.message}")
                sendMessage(KotlinChatMessage.Type.ERROR, newUser, "Failed to connect.")
            }
        }catch(e: Exception){
            sendMessage(KotlinChatMessage.Type.ERROR, message.origin, "Could not parse user's name.")
            println("Failed to create user: ${e.message}")
        }
    }

    private fun disconnectClient(message: KotlinChatMessage) {

    }

    private fun addUser(user: KotlinChatUser) {
        if(mediator.getNumOfUsers() < MAX_USERS) {
            mediator.addUser(user)
        }else{
            throw Exception("Cannot add user ${user.id} -- server is full.")
        }
    }

    private fun sendMessage(type: KotlinChatMessage.Type, user: KotlinChatUser, data: String = "") {
        sendMessage(type, user.address, data)
    }
    private fun sendMessage(type: KotlinChatMessage.Type, address: String, data: String = ""){}


    @OptIn(ExperimentalUuidApi::class)
    private fun generateUUID() : String{
        return Uuid.random().toString()
    }



}