import com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server.KotlinChatRoom
import com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server.KotlinChatUser
import com.lucaslpmoura.kotlin_chat.com.lucaslpmoura.kotlin_chat.server.UserRoomMediator
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny


import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UserRoomMediatorTest : FunSpec({

  lateinit var mediator: UserRoomMediator

  beforeTest {
    mediator = UserRoomMediator()
  }

  context("Room management") {

    test("should add a room successfully") {
      val room = KotlinChatRoom("1", "Test Room", mediator, 3)

      shouldNotThrowAny {
        mediator.addRoom(room)
      }
    }

    test("should not allow adding the same room twice") {
      val room = KotlinChatRoom("1", "Test Room", mediator, 3)

      mediator.addRoom(room)

      shouldThrow<Exception> {
        mediator.addRoom(room)
      }
    }

    test("should remove an existing room") {
      val room = KotlinChatRoom("1", "Test Room", mediator, 3)

      mediator.addRoom(room)

      shouldNotThrowAny {
        mediator.removeRoom(room)
      }
    }

    test("should not remove a room that does not exist") {
      val room = KotlinChatRoom("1", "Test Room", mediator, 3)

      shouldThrow<Exception> {
        mediator.removeRoom(room)
      }
    }
  }

  context("User management") {

    test("should add a user successfully") {
      val user = KotlinChatUser("1", "Test User", mediator)

      shouldNotThrowAny {
        mediator.addUser(user)
      }
    }

    test("should not allow adding the same user twice") {
      val user = KotlinChatUser("1", "Test User", mediator)

      mediator.addUser(user)

      shouldThrow<Exception> {
        mediator.addUser(user)
      }
    }

    test("should remove an existing user") {
      val user = KotlinChatUser("1", "Test User", mediator)

      mediator.addUser(user)

      shouldNotThrowAny {
        mediator.removeUser(user)
      }
    }

    test("should not remove a user that does not exist") {
      val user = KotlinChatUser("1", "Test User", mediator)

      shouldThrow<Exception> {
        mediator.removeUser(user)
      }
    }
  }

  context("Multiple users") {

    test("should add multiple users successfully") {
      val users = listOf(
        KotlinChatUser("1", "User 1", mediator),
        KotlinChatUser("2", "User 2", mediator),
        KotlinChatUser("3", "User 3", mediator),
        KotlinChatUser("4", "User 4", mediator),
        KotlinChatUser("5", "User 5", mediator)
      )
      shouldNotThrowAny {
        users.forEach { mediator.addUser(it) }
      }
    }
  }

  context("Checking user data") {
    val user = KotlinChatUser("1", "User 1", mediator)
    val room = KotlinChatRoom("1", "Test Room", mediator, 3)

    shouldNotThrowAny {
      mediator.addUser(user)
      mediator.addRoom(room)
      mediator.addUserToRoom(user, room)
    }

    mediator.getUserRoom(user) shouldBe room

  }

  context("Checking room data") {
    val user1 = KotlinChatUser("1", "User 1", mediator)
    var user2 = KotlinChatUser("2", "User 2", mediator)
    val room = KotlinChatRoom("1", "Test Room", mediator, 3)

    shouldNotThrowAny {
      mediator.addUser(user1)
      mediator.addUser(user2)
      mediator.addRoom(room)
      mediator.addUserToRoom(user1, room)
      mediator.addUserToRoom(user2, room)
    }

    mediator.getRoomUsers(room) shouldBe setOf(user1, user2)
  }
})