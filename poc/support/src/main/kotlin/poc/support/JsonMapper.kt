package poc.support

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonMapper {

    private lateinit var cachedMapper: Json

    val mapper = if(::cachedMapper.isInitialized) cachedMapper else Json.also { cachedMapper = it }

    inline fun <reified T> T.encode(): String = mapper.encodeToString(this)
    inline fun <reified T> String.decode(): T = mapper.decodeFromString(this)
}