import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    firstSource().flatMapLatestSuccess { first ->
        secondSource().mapSuccess { second ->
            println(" data $first - $second ")
            second - first
        }
    }.collectLatest { result -> result.onSuccess { println(it) } }


}

fun firstSource() = (0..9).asFlow().map { ResponseResult.success(it) }.onEach { delay(100) }.flowOn(Dispatchers.Default)

fun secondSource() = (0..99).asFlow().map { ResponseResult.success(it * it) }.onEach { delay(100) }.flowOn(Dispatchers.Default)

sealed class ResponseResult<T> {
    data class Success<T>(val data: T) : ResponseResult<T>()
    data class Error<T>(val error: ErrorObject) : ResponseResult<T>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun <T> error(error: ErrorObject) = Error<T>(error)
    }

    suspend fun <R> onSuccess(action: suspend (T) -> R): ResponseResult<R> =
        when (this) {
            is Success -> success(action(data))
            is Error -> error(error)
        }

    suspend fun onError(action: suspend (ErrorObject) -> Unit): ResponseResult<T> =
        also { if (this is Error) action(error) }
}

fun <T, R> Flow<ResponseResult<T>>.mapSuccess(action: suspend (T) -> R) = map { state ->
    state.onSuccess { action(it) }
}

fun <T, R> Flow<ResponseResult<T>>.flatMapLatestSuccess(action: (T) -> Flow<ResponseResult<R>>) =
    flatMapLatest { state ->
        when (state) {
            is ResponseResult.Success -> action(state.data)
            is ResponseResult.Error -> flowOf(ResponseResult.error(state.error))
        }
    }

class ErrorObject(val msg: String)