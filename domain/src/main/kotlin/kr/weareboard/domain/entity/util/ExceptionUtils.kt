package kr.weareboard.werewolf.domain.entity.util

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

fun fail(msg: String): Nothing {
    throw IllegalArgumentException(msg)
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID, msg: String = ""): T {
    return this.findByIdOrNull(id) ?: fail(msg)
}
