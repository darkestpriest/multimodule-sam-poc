package poc.datasource.impl

import poc.model.Artist
import poc.model.Song
import poc.model.Style
import poc.support.JsonMapper.decode
import poc.support.JsonMapper.encode
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun List<Map<String, AttributeValue>>.toSongs() = this.map { it.toSong() }

fun Map<String, AttributeValue>.toSong() = Song(
        name = this[DynamoDbSongTableField.NAME].toS(),
        artist = this[DynamoDbSongTableField.ARTIST].toArtist(),
        style = this[DynamoDbSongTableField.STYLE].toStyle()
)

fun Song.toAttributes() = mapOf(
        *name.toAttribute(DynamoDbSongTableField.NAME),
        *artist.toJsonAttribute(DynamoDbSongTableField.ARTIST),
        *style.toAttribute(DynamoDbSongTableField.STYLE)
)

private fun Any?.toAttribute(field: DynamoDbSongTableField) =
        this.toAttributeFor(field.lowercase())
private fun Any?.toAttributeFor(key: String) =
        (if(this != null) arrayOf(key to this.toSAttribute { it.toString() }) else arrayOf())

private fun Artist.toJsonAttribute(field: DynamoDbSongTableField) = this.encode().toAttribute(field)

private fun AttributeValue?.toArtist() = this.toS().decode<Artist>()
private fun AttributeValue?.toS() = this?.s()!!
private fun AttributeValue?.toStyle(): Style = enumValueOf(this.toS())

private fun <T> T.toSAttribute(transform: (T) -> String): AttributeValue =
        AttributeValue.builder().s(transform.invoke(this)).build()

private operator fun Map<String, AttributeValue>.get(field: DynamoDbSongTableField) = this[field.lowercase()]