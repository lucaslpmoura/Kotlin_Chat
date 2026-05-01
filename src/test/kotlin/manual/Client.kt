package manual

import com.lucaslpmoura.kotlin_chat.client.KotlinChatClient
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val client : KotlinChatClient = KotlinChatClient()
    client.run()
    delay(2.seconds)
    client.sendConnectMessage("LUCAS")


}