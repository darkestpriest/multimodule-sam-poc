package poc.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
        val name: String,
        val active: Boolean
)