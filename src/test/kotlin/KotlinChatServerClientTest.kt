import com.lucaslpmoura.kotlin_chat.client.KotlinChatClient
import com.lucaslpmoura.kotlin_chat.server.KotlinChatServer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
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

        afterTest{
            client.disconnect()
            delay(100.milliseconds)
            server.stop()
        }

        test("list rooms"){
            while(!client.isConnected){
                delay(100.milliseconds)
            }
            client.listRooms()
            delay(100.milliseconds)

            client.serverRooms.values.shouldContainAll("Room 1", "Room 2")
        }

        test("joining room"){
            while(!client.isConnected){
                delay(100.milliseconds)
            }

            client.listRooms()

            while(client.serverRooms.isEmpty()){
                delay(100.milliseconds)
            }

            val roomid = client.serverRooms.keys.elementAt(0)
            client.joinRoom(roomid)
            delay(100.milliseconds)

            client.currentRoomId shouldBe roomid
        }
    }
})