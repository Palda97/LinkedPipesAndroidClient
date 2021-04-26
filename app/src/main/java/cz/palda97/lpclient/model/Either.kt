package cz.palda97.lpclient.model

/**
 * Wrapper for two different object types.
 * Intended to be used with the keyword when.
 *
 * fun f(): Either<A, B>
 *
 * val b: B = when(val res = f()) {
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;is Either.Left -> return StatusCode.PARSING_ERROR
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;is Either.Right -> res.value
 *
 * }
 */
sealed class Either <L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>()
    data class Right<L, R>(val value: R) : Either<L, R>()
}