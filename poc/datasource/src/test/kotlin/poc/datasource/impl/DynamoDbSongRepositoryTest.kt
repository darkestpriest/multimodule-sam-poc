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
import kotlin.test.assertNull

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

    @Test
    fun `returns null on not found song by name`() {
        /*save any song in database*/saveSong()

        assertNull(sut.findBySongName(randomString()))
    }

    @Test
    fun `retrieve song by name`() {
        val expected = saveSong()
        /*save any song in database*/saveSong()

        assertEquals(
                expected, sut.findBySongName(expected.name)
        )
    }

    @Test
    fun `retrieve empty song set by not found songs by artist`() {
        /*save any song in database*/saveSong()

        assertEquals(
                emptySet(), sut.findByArtist(artist())
        )
    }

    @Test
    fun `retrieve song set found songs by artist`() {
        val artist = artist()
        val expected = 0.rangeTo(5).map {
            saveSong(artist)
        }.toSet()
        /*save any song in database*/saveSong()

        assertEquals(
                expected, sut.findByArtist(artist)
        )
    }

    private fun saveSong(artist: Artist = artist()) : Song =
            Song(
                    name = randomString(),
                    artist = artist,
                    style = Style.PROGRESSIVE_METAL
            ).also {
                sut.save(it)
            }

    private fun artist() = Artist(
            name = randomString(),
            active = true
    )
}