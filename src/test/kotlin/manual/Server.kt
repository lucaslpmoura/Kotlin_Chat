package manual

import com.lucaslpmoura.kotlin_chat.server.KotlinChatServer

suspend fun main(){

    val server = KotlinChatServer()
    server.start()
    server.run()

    while (true) {}
}