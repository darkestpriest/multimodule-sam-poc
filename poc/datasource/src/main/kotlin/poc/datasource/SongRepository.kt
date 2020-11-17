package poc.datasource

import poc.model.Artist
import poc.model.Song

interface SongRepository {
    fun findBy(names: List<String>): Set<Song>
    fun findBySongName(name: String): Song?
    fun findByArtist(artist: Artist): Set<Song>
    fun save(song: Song): Boolean
}