package com.lucaslpmoura.kotlin_chat.server

import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage.Type
import com.lucaslpmoura.kotlin_chat.common.MAX_MESSAGE_SIZE
import com.lucaslpmoura.kotlin_chat.common.SERVER_PORT
import com.lucaslpmoura.kotlin_chat.common.USER_NAME_BUFFER_SIZE
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.toByteArray
import com.sun.security.ntlm.Client

import java.net.ServerSocket
import java.net.Socket

import kotlinx.coroutines.*
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KotlinChatServer {

    @Volatile
    var state : STATE = STATE.OFFLINE
        private set

    val port : Int = SERVER_PORT
    lateinit var socket : ServerSocket

    val MAX_ROOMS : Int = 3
    // Later use -> var numOfRooms: Int = 0
    val MAX_USERS : Int = 3
    var numOfUsers: Int = 0


    val mediator: UserRoomMediatorInterface = UserRoomMediator()

    private val initialConnectionScope : CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var connectionJob : Job

    private val readScope : CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var userJobs : ConcurrentHashMap<String, Job> = ConcurrentHashMap<String, Job>()


    public fun start(){
        println("Starting server...")
        if(state != STATE.OFFLINE && state != STATE.STOPPED){
            throw Exception("Server has already started!")
        }
        socket = ServerSocket(port)
        println("Server listening at $port")
        state = STATE.STARTING
    }
    public fun run(){
        if(state != STATE.STARTING){
            throw Exception("Server has not been started!!")
        }

        state = STATE.ONLINE
        println("Starting connection coroutines...")

        connectionJob = initialConnectionScope.launch {

                while(state == STATE.ONLINE && socket.isBound) {

                    try {
                        establishConnection()
                    }catch (e: SocketException) {
                        println("Accept look finished (closed socket)")
                        break
                    } catch (e : Exception) {
                        println("Error estabilishing connection: ${e.message}")
                    }

                }

        }
        println("Connection coroutines started!")

        mediator.addRoom(KotlinChatRoom(generateUUID(), "Room 1", mediator, 3))
        mediator.addRoom(KotlinChatRoom(generateUUID(), "Room 2", mediator, 3))


        println("Server is online.")
    }

    public suspend fun stop(){
        if(state == STATE.OFFLINE || state == STATE.STOPPED){
            throw Exception("Server is already stopped!")
        }

        println("Stopping server...")
        socket.close()
        println("Server socket closed.")

        connectionJob.cancelAndJoin()

        initialConnectionScope.cancel()
        for(job in userJobs.values){
            job.cancel()
        }
        mediator.removeAllRooms()
        mediator.removeAllUsers()


        state = STATE.STOPPED
        println("Server stopped.")

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
            throw e
        }
    }

    private fun sendMessage(user: KotlinChatUser, type: KotlinChatMessage.Type,  data: String = "") {
        println("Sending message of type ${type.name} to user ${user.id}")

        val message = constructMessage(user, type, mapOf("data" to data))
        try{
            user.output?.write(message.toByteArray())
        }catch(e: Exception){
            println("Failed to send ${type.name} message to user ${user.id}: ${e.message}")
        }
    }

    /*
    private fun sendTextMessage(room: KotlinChatRoom, originUser: KotlinChatUser, type: KotlinChatMessage.Type,  text: String = "") {
        for(user in mediator.getRoomUsers(room)){
            val data =
            sendMessage(user, Type.TEXT, data)
        }
    }

     */

    private fun processReceivedMessage(message: KotlinChatMessage) {
        println("TYPE: ${message.type.name}")
        when (message.type) {
            KotlinChatMessage.Type.DISCONNECT -> disconnectClient(message)
            KotlinChatMessage.Type.LIST_ROOMS -> sendRoomList(message)
            KotlinChatMessage.Type.JOIN_ROOM -> addUserToRoom(message)
            KotlinChatMessage.Type.LEAVE_ROOM -> removeUserFromRoom(message)
            KotlinChatMessage.Type.TEXT -> processText(message)
            KotlinChatMessage.Type.ERROR -> TODO()
            else -> {
                println("Failed to process message of type ${message.type}")
                return
            }
        }
    }

    private fun constructMessage(user: KotlinChatUser, type: KotlinChatMessage.Type, data: Map<String,String> = mapOf<String, String>()): KotlinChatMessage {
        lateinit var messageData: String
        when(type) {
            KotlinChatMessage.Type.CONNECT -> messageData = user.id
            KotlinChatMessage.Type.DISCONNECT -> messageData = ""
            KotlinChatMessage.Type.LIST_ROOMS -> messageData = mountRoomListMessageData()
            KotlinChatMessage.Type.JOIN_ROOM -> messageData = data["data"]!!
            KotlinChatMessage.Type.LEAVE_ROOM -> messageData = data["data"]!!
            KotlinChatMessage.Type.TEXT -> TODO()

            // ERROR
            else -> {messageData = data["data"]!!}
        }
        return KotlinChatMessage(user.id, type, messageData)
    }

    private fun mountRoomListMessageData() : String {
        var data : String =  ""
        for(room in mediator.getAllRooms()){
            data += room.id + '|' + room.name + '|'
        }
        data = data.dropLast(1) // Removes the last '|'
        return data
    }

    private fun disconnectClient(message: KotlinChatMessage) {
        try{
            val userId = parseDataFromClientMessage(message)["id"]!!
            val user = mediator.getUserById(userId)
            if(message.origin == "SERVER" || message.origin == user.address){
                removeUser(user)
                sendMessage(user, KotlinChatMessage.Type.DISCONNECT)
            }else{
                throw Exception("You don't have permission do disconnect users.")
            }
        }catch(e: Exception){
            println("Failed to disconnect user: ${e.message}")

        }
    }

    private fun sendRoomList(message: KotlinChatMessage) {
        lateinit var userId: String
        lateinit var user : KotlinChatUser
        try{
            userId = parseDataFromClientMessage(message)["id"]!!
            user = mediator.getUserById(userId)
            sendMessage(user, KotlinChatMessage.Type.LIST_ROOMS)
        }catch(e: Exception){
            println("Failed to send room list to user $userId: ${e.message}")
            sendMessage(user, KotlinChatMessage.Type.ERROR, "Error joining room.")
        }
    }

    private fun addUserToRoom(message: KotlinChatMessage) {
        lateinit var userId: String
        lateinit var roomId: String
        lateinit var user : KotlinChatUser
        try{
            val data = parseDataFromClientMessage(message)
            userId = data["userId"]!!
            roomId = data["roomId"]!!

            user = mediator.getUserById(userId)
            val room = mediator.getRoomById(roomId)
            mediator.addUserToRoom(user, room)

            sendMessage(user, KotlinChatMessage.Type.JOIN_ROOM, roomId)
        }catch(e: Exception){
            println("Failed to add user $userId to room $roomId: ${e.message}")
            sendMessage(user, KotlinChatMessage.Type.ERROR, "Error joining room $roomId.")
        }
    }

    private fun removeUserFromRoom(message: KotlinChatMessage) {
        lateinit var userId: String
        lateinit var roomId: String
        lateinit var user : KotlinChatUser
        try{
            val data = parseDataFromClientMessage(message)
            userId = data["userId"]!!
            roomId = data["roomId"]!!

            user = mediator.getUserById(userId)
            val room = mediator.getRoomById(roomId)
            mediator.removeUserFromRoom(user, room)

            sendMessage(user, KotlinChatMessage.Type.LEAVE_ROOM, roomId)
        }catch(e: Exception){
            println("Failed to remove user $userId from room $roomId: ${e.message}")
            sendMessage(user, KotlinChatMessage.Type.ERROR, "Error leaving room $roomId.")
        }
    }


    private fun processText(message: KotlinChatMessage) {
        lateinit var userId: String
        lateinit var roomId: String
        lateinit var user : KotlinChatUser
        try{
            val data = parseDataFromClientMessage(message)
            userId = data["userId"]!!
            roomId = data["roomId"]!!
            val text = data["text"]!!
            val user = mediator.getUserById(userId)
            val room = mediator.getRoomById(roomId)

            if(message.origin != user.address){
                throw Exception("User ${message.origin} does not have permission to disconnect other users.")
            }
            if(!mediator.isUserInRoom(user, room)) {
                throw Exception("User $userId is not in room $roomId")
            }



        }catch(e: Exception){
            println("Failed to send room list to user $userId: ${e.message}")
            sendMessage(user, KotlinChatMessage.Type.ERROR, "Error sending text to room $roomId.")
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
            println("Connected users: ${mediator.getNumOfUsers()}.")
        }else{
            throw Exception("Cannot remove user ${user.id} -- user is not on server.")
        }
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

    private fun parseDataFromClientMessage(message : KotlinChatMessage) : Map<String, String>{
        return when(message.type) {
            Type.CONNECT -> {
                if(message.data.isEmpty()){
                    throw Exception("name not present.")
                }
                mapOf("name" to message.data)
            }
            Type.DISCONNECT, Type.LIST_ROOMS -> {
                if(message.data.isEmpty()){
                    throw Exception("id not present.")
                }
                mapOf("id" to message.data)
            }
            Type.JOIN_ROOM, Type.LEAVE_ROOM -> {
                val ids = mutableMapOf<String,String>()
                if(message.data.isEmpty()){
                    throw Exception("no data provided.")
                }

                val splitData = message.data.split('|')
                println(splitData)
                if(splitData.size != 2){
                    throw Exception("room or user id missing.")
                }

                ids["userId"] = splitData[0]
                ids["roomId"] = splitData[1]

                ids.toMap()
            }

            Type.TEXT -> {
                val data = mutableMapOf<String,String>()
                if(message.data.isEmpty()){
                    throw Exception("no data provided.")
                }

                val splitData = message.data.split('|')
                println(splitData)
                if(splitData.size != 3){
                    throw Exception("room id , user id or text missing.")
                }

                data["userId"] = splitData[0]
                data["roomId"] = splitData[1]
                data["text"] = splitData[2]

                data.toMap()
            }
            else -> {
                mapOf<String, String>("content" to "")
            }
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
                    processReceivedMessage(message)
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

    public enum class STATE{
        OFFLINE, STARTING, ONLINE, STOPPED
    }
}