import com.lucaslpmoura.kotlin_chat.client.KotlinChatClient
import com.lucaslpmoura.kotlin_chat.server.KotlinChatServer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KotlinChatServerClientTest : FunSpec({


    //context("starting session"){}
    //context("ending session"){}
    context("session commands") {

        lateinit var client : KotlinChatClient
        lateinit var server: KotlinChatServer

        beforeEach{
            println("BEFORE TEST")
            server = KotlinChatServer()
            server.start()
            server.run()
            client = KotlinChatClient()
            delay(100.milliseconds) // Giving time for server to start
            client.run()

            shouldNotThrowAny {
                println("[TEST] Connecting to server...")
                delay(100.milliseconds)
                client.connect("João da Silva")

            }

        }

        afterEach{
            println("AFTER TEST")
            client.disconnect()
            delay(100.milliseconds)
            server.stop()
            delay(2.seconds)
        }

        context("LIST_ROOMS"){
            test("list rooms").config(invocations = 1) {
                while(!client.isConnected){
                    delay(100.milliseconds)
                    println(client.isConnected)
                }
                client.listRooms()
                delay(100.milliseconds)

                client.serverRooms.values.shouldContainAll("Room 1", "Room 2")
            }
        }

        context("JOIN_ROOM"){
            test("joining room"){
                while(!client.isConnected){
                    delay(100.milliseconds)

                }

                client.listRooms()

                while(client.serverRooms.isEmpty()){
                    delay(100.milliseconds)
                }

                val roomId = client.serverRooms.keys.elementAt(0)
                client.joinRoom(roomId)
                delay(100.milliseconds)

                client.currentRoomId shouldBe roomId
            }
        }

        context("LEAVE_ROOM"){
            test("leaving room"){
                while(!client.isConnected){
                    delay(100.milliseconds)
                }

                client.listRooms()

                while(client.serverRooms.isEmpty()){
                    delay(100.milliseconds)
                }

                val roomId = client.serverRooms.keys.elementAt(0)
                client.joinRoom(roomId)
                delay(100.milliseconds)

                client.currentRoomId shouldBe roomId

                client.leaveRoom()
                delay(100.milliseconds)

                client.currentRoomId shouldBe null
            }
        }
    }
})