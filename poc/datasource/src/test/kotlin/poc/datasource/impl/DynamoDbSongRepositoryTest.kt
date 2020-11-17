package poc.datasource.impl

import org.junit.Before
import org.junit.Test
import poc.datasource.DynamoDbTestContainer.songTable
import poc.datasource.SongRepository
import poc.datasource.client
import poc.datasource.randomString
import poc.model.Artist
import poc.model.Song
import poc.model.Style
import kotlin.test.assertEquals

class DynamoDbSongRepositoryTest {

    companion object {
        private val dynamoDbClient = client()
        private const val tableName = songTable
    }

    private lateinit var sut: SongRepository

    @Before
    fun setup() {
        sut = DynamoDbSongRepository(
                dynamoDbClient = dynamoDbClient,
                table = tableName
        )
    }

    @Test
    fun `can retrieve a list of song by name list`() {
        val expected = 0.rangeTo(5).map {
            saveSong()
        }.toSet()
        val names = expected.map { it.name }

        assertEquals(
                expected, sut.findBy(names)
        )
    }

    private fun saveSong() : Song =
            Song(
                    name = randomString(),
                    artist = artist(),
                    style = Style.PROGRESSIVE_METAL
            ).also {
                sut.save(it)
            }

    private fun artist() = Artist(
            name = randomString(),
            active = true
    )
}