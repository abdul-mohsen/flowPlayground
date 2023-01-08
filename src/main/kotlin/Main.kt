import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis


fun main(): Unit = runBlocking {

    val time = measureTimeMillis {
        counter().collect {
            println(it)
        }
    }

    println("$time ms")
}

fun counter() = (1..Int.MAX_VALUE).asFlow()
    .onEach { delay(1000) }

fun text() = flow {
    while (true) {
        val text = File("src/example.txt").readText()
        emit(text)
    }
}
    .onEach { delay(100) }
    .onEach { if (it == "ssda") throw Exception() }
    .distinctUntilChanged()
    .catch { e -> emit(e.stackTraceToString()) }
    .flowOn(Dispatchers.IO)