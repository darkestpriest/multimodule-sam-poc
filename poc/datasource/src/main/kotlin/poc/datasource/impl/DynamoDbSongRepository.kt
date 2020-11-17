package poc.datasource.impl

import poc.datasource.SongRepository
import poc.model.Artist
import poc.model.Song
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes

class DynamoDbSongRepository(
        private val dynamoDbClient: DynamoDbClient,
        private val table: String
) : SongRepository {

    companion object {
        const val secondaryIndexName = "artist_idx"

        private const val artistKey = ":art"
    }

    override fun findBy(names: List<String>): Set<Song> =
            names.map {
                mapOf("name" to AttributeValue.builder().s(it).build())
            }. let {
                dynamoDbClient.batchGetItem { builder -> builder
                        .requestItems(
                                mapOf(
                                        table to KeysAndAttributes.builder().keys(
                                                *it.toTypedArray()
                                        ).build()
                                )
                        )

                }
            }.let { response ->
                response.takeIf { it.hasResponses() }?.let {
                    it.responses()[table]?.toSongs()
                }?.toSet() ?: emptySet()
            }

    override fun findBySongName(name: String): Song? =
            dynamoDbClient.getItem { it
                    .tableName(table)
                    .key(mapOf(*name.toAttribute(DynamoDbSongTableField.NAME)))
            }.let { response ->
                response.takeIf { it.hasItem() }?.item()?.toSong()
            }

    override fun findByArtist(artist: Artist): Set<Song> =
            dynamoDbClient.queryPaginator { it
                    .tableName(table).indexName(secondaryIndexName)
                    .keyConditionExpression("${DynamoDbSongTableField.ARTIST.lowercase()} = $artistKey")
                    .expressionAttributeValues(mapOf(artistKey to artist.toSAttribute { a -> a.toJsonString() }))
            }.items()?.toList()?.toSongs()?.toSet()
                    ?: emptySet()

    override fun save(song: Song): Boolean =
            dynamoDbClient.putItem { it
                    .tableName(table)
                    .item(song.toAttributes())
            }.let {
                true
            }
}