import com.lucaslpmoura.kotlin_chat.common.KotlinChatMessage
import com.lucaslpmoura.kotlin_chat.common.parseMessageFromBytes
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.awt.TrayIcon


class KotlinChatMessageTest : FunSpec({

    context("message parsing from bytes") {
        test("CONNECT") {
            val origin = "localhost"
            val type = KotlinChatMessage.Type.CONNECT
            val data = "João da Silva"
            val content = "${type.name}|$data"
            val bytes = content.toByteArray()

            val message = parseMessageFromBytes(origin, bytes, bytes.size)
            message.origin shouldBe origin
            message.type shouldBe type
            message.data shouldBe data


        }
    }
})