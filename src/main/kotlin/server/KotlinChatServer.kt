package com.lucaslpmoura.kotlin_chat.server

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.USER_NAME_BUFFER_SIZE
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.parseDataFromClientMessage
import com.lucaslpmoura.kotlin_chat.common.toByteArray

import java.net.ServerSocket
import java.net.Socket

import kotlinx.coroutines.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KotlinChatServer {

    val port : Int = 7960
    val socket : ServerSocket = ServerSocket(port)



    val MAX_ROOMS : Int = 3
    // Later use -> var numOfRooms: Int = 0
    val MAX_USERS : Int = 3
    var numOfUsers: Int = 0


    val mediator: UserRoomMediatorInteface = UserRoomMediator()

    private val initialConnectionScope : CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val readScope : CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val userJobs : MutableMap<String, Job> = mutableMapOf<String, Job>()

    public fun run(){
        initialConnectionScope.launch {
            launch {
                while(true){
                    establishConnection()
                }
            }
        }


    }

    private fun establishConnection() {
        println("Accepting TCP connections...")

        try{
            val clientSocket : Socket = socket.accept()
            val newUser = KotlinChatUser(
                id = generateUUID(),
                name = "CONNECTING_USER",
                socket = clientSocket,
                mediator = mediator,
            )

            println("Client at ${newUser.address} is connecting with id ${newUser.id}.")

            try{
                newUser.name = readUserName(newUser)
                addUser(newUser)
                println("User ${newUser.id} connected as ${newUser.name}.")

                sendMessage(newUser, KotlinChatMessage.Type.CONNECT)
            }catch (e: Exception){
                println("Failed to add user: ${e.message}")
                sendMessage(newUser,KotlinChatMessage.Type.ERROR, "Could not connect to server.")
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
            val userId = parseDataFromClientMessage(message)["id"]!!
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
            createUserJob(user)
        }else{
            throw Exception("Cannot add user ${user.id} -- server is full.")
        }
    }

    private fun removeUser(user: KotlinChatUser) {
        if(mediator.isUserConnected(user)){
            mediator.removeUser(user)
            deleteUserJob(user)
        }else{
            throw Exception("Cannot remove user ${user.id} -- user is not on server.")
        }
    }

    private fun sendMessage(user: KotlinChatUser, type: KotlinChatMessage.Type,  data: String = "") {
        println("Sending message of type ${type.name} to user ${user.id}")

        val message = constructMessage(user, type, data)
        try{
            user.output?.write(message.toByteArray())
        }catch(e: Exception){
            println("Failed to send ${type.name} message to user ${user.id}: ${e.message}")
        }
    }

    private fun constructMessage(user: KotlinChatUser, type: KotlinChatMessage.Type, data: String = ""): KotlinChatMessage {
        val origin = "SERVER"
        lateinit var data: String
        when(type) {
            KotlinChatMessage.Type.CONNECT -> data = user.id
            KotlinChatMessage.Type.DISCONNECT -> TODO()
            KotlinChatMessage.Type.AFK -> TODO()
            KotlinChatMessage.Type.LIST_ROOMS -> TODO()
            KotlinChatMessage.Type.JOIN_ROOM -> TODO()
            KotlinChatMessage.Type.LEAVE_ROOM -> TODO()
            KotlinChatMessage.Type.TEXT -> TODO()
            KotlinChatMessage.Type.ERROR -> TODO()
        }
        return KotlinChatMessage(origin, type, data)
    }

    private fun readUserName(user: KotlinChatUser): String {
        try{
            val buffer = ByteArray("CONNECT|".length + USER_NAME_BUFFER_SIZE)

            val bytesRead = user.input?.read(buffer) ?: throw Exception("Failed to read input from user ${user.id}.")

            if (bytesRead == -1) throw Exception("Failed to read input from user ${user.id} -- input length is 0.")

            val messageFromBytes = getMessageFromBytes(user.address, buffer)

            return parseDataFromClientMessage(messageFromBytes)["name"] ?: throw Exception("Could not read user's name!")
        }catch(e: Exception){
            throw Exception("Failed to read user name: ${e.message}")
        }
    }

    private fun createUserJob(user: KotlinChatUser) {
        val userJob = readScope.launch {
            println("Creating job for user ${user.id}.")
            var readError = false
            while(!readError) {
                try{
                    val buffer = ByteArray(MAX_MESSAGE_SIZE)
                    val bytesRead = user.input?.read(buffer) ?: throw Exception("Failed to read input from user ${user.id}.")
                    if (bytesRead == -1) throw Exception("Failed to read input from user ${user.id} -- input length is 0.")
                    val message = getMessageFromBytes(user.address, buffer)
                    processMessage(message)
                }catch(e: Exception){
                    println("Failed to read input from user ${user.id}: ${e.message} ")
                    readError = true
                    deleteUserJob(user)
                }
            }


        }
        userJobs[user.id] = userJob
    }

    private fun deleteUserJob(user: KotlinChatUser) {
        for(id in userJobs.keys){
            if(id == user.id){
                userJobs[id]?.cancel()
                userJobs.remove(id)
                println("Stopping job for user ${user.id}.")
            }
        }
    }




    @OptIn(ExperimentalUuidApi::class)
    private fun generateUUID() : String{
        return Uuid.random().toString()
    }
}