import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.getMessageFromBytes
import com.lucaslpmoura.kotlin_chat.common.toByteArray

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe



class KotlinChatMessageTest : FunSpec({

    context("deserializing message from bytes") {
        lateinit var origin : String
        lateinit var data: String

        beforeTest {
            origin = "localhost"
            data = "João da Silva"
        }
        test("CONNECT") {

            val type = KotlinChatMessage.Type.CONNECT

            val content = "${type.name}|$data"
            val bytes = content.toByteArray() + 0.toByte()

            val message = getMessageFromBytes(origin, bytes)
            message.origin shouldBe origin
            message.type shouldBe type
            message.data shouldBe data


        }

    }

    context("serializing message object"){
        lateinit var origin : String
        lateinit var data: String

        beforeTest {
            origin = "localhost"
            data = "A1B2C3D4E5"
        }

        test("CONNECT"){
            val type = KotlinChatMessage.Type.CONNECT
            val message = KotlinChatMessage(origin, type, data)
            val messageString = "${type.name}|$data"

            val bytes = message.toByteArray()

            bytes shouldBe messageString.toByteArray()

        }
    }
})