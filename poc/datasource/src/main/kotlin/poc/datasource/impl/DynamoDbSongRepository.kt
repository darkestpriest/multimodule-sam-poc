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

    override fun findBySongName(name: String): Song? {
        TODO("Not yet implemented")
    }

    override fun findByArtist(artist: Artist): Set<Song> {
        TODO("Not yet implemented")
    }

    override fun save(song: Song): Boolean =
            dynamoDbClient.putItem { it
                    .tableName(table)
                    .item(song.toAttributes())
            }.let {
                true
            }
}