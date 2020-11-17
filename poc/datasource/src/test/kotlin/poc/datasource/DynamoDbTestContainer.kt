package poc.datasource

import org.junit.AssumptionViolatedException
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.localstack.LocalStackContainer
import poc.datasource.DynamoDbTestContainer.songTable
import poc.datasource.impl.DynamoDbSongTableField
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI

lateinit var cachedDynamoClient: DynamoDbClient
lateinit var cachedContainer: LocalStackContainer

object DynamoDbTestContainer {
    const val songTable = "table"
}

fun client(): DynamoDbClient =
        initClient(block = ::createTable)

private fun createTable(client: DynamoDbClient) {
    val createTableRequest = CreateTableRequest
            .builder()
            .tableName(songTable)
            .attributeDefinitions(
                    AttributeDefinition
                            .builder()
                            .attributeName(DynamoDbSongTableField.NAME.lowercase())
                            .attributeType(ScalarAttributeType.S).build()
            )
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(100).writeCapacityUnits(100).build())
            .keySchema(
                    KeySchemaElement.builder().attributeName(DynamoDbSongTableField.NAME.lowercase()).keyType(KeyType.HASH).build()
            )
            .build()
    client.createTable(createTableRequest)
    waitForTableToBeCreated(client)
}

private fun initContainer(): LocalStackContainer =
        if(!::cachedContainer.isInitialized) {
            LocalStackContainer()
                    .withServices(LocalStackContainer.Service.DYNAMODB)
                    .apply { start() }
                    .also { cachedContainer = it }
        } else {
            cachedContainer
        }

private fun initClient() =
        if(!::cachedDynamoClient.isInitialized) {
            val port = 4569

            val container = initContainer()

            DynamoDbClient
                    .builder()
                    .endpointOverride(URI.create("http://${container.containerIpAddress}:${container.getMappedPort(port)}"))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
                    .region(Region.US_EAST_2)
                    .build()
        } else {
            cachedDynamoClient
        }

private fun initClient(blockName: String = "createTable", block: (DynamoDbClient) -> Unit) =
        try {
            initClient().also {
                block(it)
            }
        } catch (e: ContainerLaunchException) {
            throw AssumptionViolatedException("cannot init $blockName table because: ${e.message}", e)
        }

private fun waitForTableToBeCreated(client: DynamoDbClient, table: String = songTable) {
    (1..3).find {
        val status = client.describeTable(DescribeTableRequest.builder().tableName(table).build()).table().tableStatus()
        val isReady = status == TableStatus.ACTIVE
        if (!isReady) Thread.sleep(500)
        isReady
    } ?: throw IllegalStateException("table $table is not ready yet")
}