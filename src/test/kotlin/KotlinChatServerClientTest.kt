import com.lucaslpmoura.kotlin_chat.client.KotlinChatClient
import com.lucaslpmoura.kotlin_chat.server.KotlinChatServer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class KotlinChatServerClientTest : FunSpec({

    //context("starting session"){}
    //context("ending session"){}
    context("session commands") {
        lateinit var client : KotlinChatClient
        lateinit var server: KotlinChatServer

        beforeTest{
            server = KotlinChatServer()
            server.run()
            client = KotlinChatClient()
            delay(100.milliseconds) // Giving time for server to start
            client.run()

            shouldNotThrowAny {
                delay(100.milliseconds)
                client.connect("João da Silva")
            }
        }

        test("list rooms"){
            while(!client.isConnected){
                delay(100.milliseconds)
            }
            client.listRooms()
            delay(100.milliseconds)

            client.serverRooms.values.shouldContainAll("Room 1", "Room 2")
        }
    }
})