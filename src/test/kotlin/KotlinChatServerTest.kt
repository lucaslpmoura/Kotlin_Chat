import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.server.KotlinChatServer
import io.kotest.assertions.throwables.shouldNotThrowAny

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


class KotlinChatServerTest : FunSpec({
    lateinit var server : KotlinChatServer

    beforeTest {
        server = KotlinChatServer()
    }

    context("connecting users") {
        test("should be able to connect user") {
            val message = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.CONNECT,
                data = "João da Silva"
            )
            shouldNotThrowAny {
                server.processMessage(message)
            }

            server.mediator.getNumOfUsers() shouldBe 1
        }

        test("should not connect users with empty names"){
            val message = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.CONNECT,
                data = ""
            )
            shouldNotThrowAny {
                server.processMessage(message)
            }

            server.mediator.getNumOfUsers() shouldBe 0
        }

        test("should not connect users when server is full"){
            val messages = mutableListOf<KotlinChatMessage>()

            for(i in 0..server.MAX_USERS  ){
                messages.add(KotlinChatMessage(
                    origin = "192.168.0.$i",
                    type = KotlinChatMessage.Type.CONNECT,
                    data = "User $i"
                ))
            }

            shouldNotThrowAny {
                for (message in messages) {
                    server.processMessage(message)
                }
            }

            server.mediator.getNumOfUsers() shouldBe 3
        }
    }

    context("disconnecting users"){
        test("should disconnect user when it requests it"){}
        test("should not disconnect user on message with empty id"){
            val connectMessage = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.CONNECT,
                data = "João da Silva"
            )
            val disconnectMessage = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.DISCONNECT,
                data = ""
            )
            shouldNotThrowAny {
                server.processMessage(connectMessage)
                server.processMessage(disconnectMessage)
            }

            server.mediator.getNumOfUsers() shouldBe 1
        }
        test("should not disconnect non-connected user"){
            val connectMessage = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.CONNECT,
                data = "João da Silva"
            )
            val disconnectMessage = KotlinChatMessage(
                origin = "localhost",
                type = KotlinChatMessage.Type.DISCONNECT,
                data = "NON_VALID_ID"
            )
            shouldNotThrowAny {
                server.processMessage(connectMessage)
                server.processMessage(disconnectMessage)
            }

            server.mediator.getNumOfUsers() shouldBe 1
        }
        test("should not disconnect user on other user request"){}
    }
})