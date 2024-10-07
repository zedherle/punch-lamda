package punchworld

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

class DynamoService {
    private val dynamoDbClient: DynamoDbClient = DynamoDbClient.builder()
        .region(Region.US_EAST_1)  // Replace with your desired region
        .build()

    fun getAll(): List<PunchData> {
        val scanRequest = ScanRequest.builder().tableName("punch").build()
        val scanResponse: ScanResponse = dynamoDbClient.scan(scanRequest)

        val items = scanResponse.items().map { item ->
            PunchData(
                id = item["id"]?.s() ?: "",
                createdBy = item["createdBy"]?.s() ?: "",
                createdAt = item["createdAt"]?.s() ?: "",
                imgUrl = item["imgUrl"]?.l()?.map { it.s() } ?: emptyList(),
                modifiedBy = item["modifiedBy"]?.s() ?: "",
                modifiedAt = item["modifiedAt"]?.s() ?: "",
                summary = item["summary"]?.s() ?: "",
                status = item["status"]?.s() ?: "")
        }
        return items
    }

    fun getAll(modifiedAt: String): List<PunchData> {
        val queryRequest = QueryRequest.builder()
            .tableName("punch")
            .keyConditionExpression("#modifiedAt >= :modifiedAt")
            .expressionAttributeNames(mapOf("#modifiedAt" to "modifiedAt"))
            .expressionAttributeValues(mapOf(":modifiedAt" to AttributeValue.builder().s(modifiedAt).build()))
            .build()

        val queryResult = dynamoDbClient.query(queryRequest)

        val items = queryResult.items().map { item ->
            PunchData(
                id = item["id"]?.s() ?: "",
                createdBy = item["createdBy"]?.s() ?: "",
                createdAt = item["createdAt"]?.s() ?: "",
                imgUrl = item["imgUrl"]?.l()?.map { it.s() } ?: emptyList(),
                modifiedBy = item["modifiedBy"]?.s() ?: "",
                modifiedAt = item["modifiedAt"]?.s() ?: "",
                summary = item["summary"]?.s() ?: "",
                status = item["status"]?.s() ?: "")
        }
        return items
    }

    fun getById(id: String): PunchData? {
        val getItemRequest = GetItemRequest.builder()
            .tableName("punch")
            .key(mapOf("id" to AttributeValue.builder().s(id).build()))
            .build()

        val getItemResult = dynamoDbClient.getItem(getItemRequest)

        return getItemResult.item()?.let { item ->
            PunchData(
                id = item["id"]?.s() ?: "",
                createdBy = item["createdBy"]?.s() ?: "",
                createdAt = item["createdAt"]?.s() ?: "",
                imgUrl = item["imgUrl"]?.l()?.map { it.s() } ?: emptyList(),
                modifiedBy = item["modifiedBy"]?.s() ?: "",
                modifiedAt = item["modifiedAt"]?.s() ?: "",
                summary = item["summary"]?.s() ?: "",
                status = item["status"]?.s() ?: ""
            )
        }
    }
    fun createPunches(punchDataList: List<PunchData>): List<String> {
        val writeRequests = punchDataList.map { punchData ->
            WriteRequest.builder()
                .putRequest(PutRequest.builder()
                    .item(mapOf(
                        "id" to AttributeValue.builder().s(punchData.id).build(),
                        "createdBy" to AttributeValue.builder().s(punchData.createdBy).build(),
                        "createdAt" to AttributeValue.builder().s(punchData.createdAt).build(),
                        "imgUrl" to AttributeValue.builder().l(punchData.imgUrl.map { AttributeValue.builder().s(it).build() }).build(),
                        "modifiedBy" to AttributeValue.builder().s(punchData.modifiedBy).build(),
                        "modifiedAt" to AttributeValue.builder().s(punchData.modifiedAt).build(),
                        "summary" to AttributeValue.builder().s(punchData.summary).build()
                    ))
                    .build()
                )
                .build()
        }

        val batchWriteItemRequest = BatchWriteItemRequest.builder()
            .requestItems(mapOf("punch" to writeRequests))
            .build()

        val batchWriteItemResult = dynamoDbClient.batchWriteItem(batchWriteItemRequest)

        val unprocessedItems = batchWriteItemResult.unprocessedItems()
        if (unprocessedItems.isNotEmpty()) {
            // Handle unprocessed items
        }

        return punchDataList.map { it.id }
    }

    fun updatePunchStatus(id: String, status: String): String {
        val updateItemRequest = UpdateItemRequest.builder()
            .tableName("punch")
            .key(mapOf("id" to AttributeValue.builder().s(id).build()))
            .updateExpression("set #status = :status")
            .expressionAttributeNames(mapOf("#status" to "status"))
            .expressionAttributeValues(mapOf(":status" to AttributeValue.builder().s(status).build()))

        .build()

        dynamoDbClient.updateItem(updateItemRequest)

        return id
    }
}
