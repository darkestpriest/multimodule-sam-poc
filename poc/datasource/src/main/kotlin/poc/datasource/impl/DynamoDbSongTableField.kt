package poc.datasource.impl

enum class DynamoDbSongTableField {
    NAME, ARTIST, STYLE;

    fun lowercase(): String = this.name.toLowerCase()
}