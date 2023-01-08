import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlin.system.measureTimeMillis


fun main(): Unit = runBlocking {
    val event = Channel<String>()

//    val producer = postUserInput()
//    repeat(1) { collectEvent(it, producer) }
    spamMsg(event)
    collectEvent(1, event)
}

fun CoroutineScope.produceUserInput() = produce {
    while (true) {
        val userInput = readlnOrNull()
        userInput?.let { send(it) }
    }
}

fun CoroutineScope.collectEvent(id: Int, channel: ReceiveChannel<String>) = launch {
    for (msg in channel) {
        println("PID: $id | $msg")
    }
}

fun CoroutineScope.spamMsg(channel: SendChannel<String>) = launch(Dispatchers.Default) {
    val time = measureTimeMillis {
        (1..10000).forEach {
            channel.trySend("hi $it")
        }
    }
    println("$time ms")
}

fun CoroutineScope.sendString(channel: SendChannel<String>) = launch(Dispatchers.IO) {
    while (true) {
        readlnOrNull()?.let { msg -> channel.send(msg) }
    }
}