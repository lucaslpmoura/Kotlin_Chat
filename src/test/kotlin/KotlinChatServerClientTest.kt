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
        lateinit var client1 : KotlinChatClient
        lateinit var server: KotlinChatServer

        beforeEach{
            println("[TEST] Creating variables...")
            server = KotlinChatServer()
            server.start()
            server.run()
            client = KotlinChatClient()
            delay(100.milliseconds) // Giving time for server to start
            client.run()

            client1 = KotlinChatClient()
            delay(100.milliseconds) // Giving time for server to start
            client1.run()

            shouldNotThrowAny {
                println("[TEST] Connecting to server...")
                delay(100.milliseconds)
                client.connect("João da Silva")
                delay(100.milliseconds)
                client1.connect("José dos Santos")
                delay(100.milliseconds)

            }

        }

        afterEach{
            println("AFTER TEST")
            client.disconnect()
            delay(100.milliseconds)
            client1.disconnect()
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

        context("TEXT"){
            test("sending/reading text message"){
                client.listRooms()
                client1.listRooms()

                while(client.serverRooms.isEmpty() && client1.serverRooms.isEmpty()){
                    delay(100.milliseconds)
                }


                client.joinRoom(client.serverRooms.keys.elementAt(0))
                client1.joinRoom(client1.serverRooms.keys.elementAt(0))
                delay(100.milliseconds)

                client.currentRoomId shouldBe client1.currentRoomId

                val text = "Olá!"

                client.text(text)
                delay(100.milliseconds)


                println("[TEST] Expected origin: ${client.name}")
                println("[TEST] Expected data: ${text}")
                client1.lastText!!.origin shouldBe client.name
                client1.lastText!!.data  shouldBe text
            }

        }
    }
})