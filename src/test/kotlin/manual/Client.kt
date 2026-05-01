package manual

import com.lucaslpmoura.kotlin_chat.client.KotlinChatClient
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val clients = listOf<KotlinChatClient>(KotlinChatClient(), KotlinChatClient(), KotlinChatClient(), KotlinChatClient())

    for (client in clients) {
        client.run()
    }

    delay(1.seconds)
    for(client in clients) {
        client.connect("LUCAS@${client.hashCode()}")
        delay(1000.milliseconds)
    }

    var i = 0
    while(i < 10){
        for(client in clients){
            if(client.lastError != null){
                println("[${client.id}] -- ${client.lastError?.data}")
            }
        }
        i++
        delay(100.milliseconds)
    }


}