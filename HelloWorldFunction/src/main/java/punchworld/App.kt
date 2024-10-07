package punchworld

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class App : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private val dynamoService = DynamoService()
    private val gson: Gson = Gson()

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val logger = context.logger

        return try {
            when (input.httpMethod) {
                "GET" -> {
                    val id = input.pathParameters?.get("id")
                    val modifiedId = input.pathParameters?.get("modifiedAt")
                    if (id != null) {
                        val punchData = dynamoService.getById(id)
                        return APIGatewayProxyResponseEvent().apply {
                            statusCode = 200
                            headers = mapOf("Content-Type" to "application/json")
                            body = gson.toJson(punchData)
                        }
                    }
                    else if(modifiedId!=null)
                    {
                        val punchData = dynamoService.getAll(modifiedId)
                        return APIGatewayProxyResponseEvent().apply {
                            statusCode = 200
                            headers = mapOf("Content-Type" to "application/json")
                            body = gson.toJson(punchData)
                        }
                    }
                    else {
                        val punchDataList = dynamoService.getAll()
                        return APIGatewayProxyResponseEvent().apply {
                            statusCode = 200
                            headers = mapOf("Content-Type" to "application/json")
                            body = gson.toJson(punchDataList)
                        }
                    }
                }
                "POST" -> {
                    val punchDataList: List<PunchData> = gson.fromJson(input.body, object : TypeToken<List<PunchData>>() {}.type)
                        val createdIds = dynamoService.createPunches(punchDataList)
                        return APIGatewayProxyResponseEvent().apply {
                            statusCode = 201
                            headers = mapOf("Content-Type" to "application/json")
                            body = """{"message": "Punches created successfully", "ids": $createdIds}"""
                    }
                }
                "PUT" -> {
                    // Handle PUT requests
                    return APIGatewayProxyResponseEvent().apply {
                        statusCode = 200
                        headers = mapOf("Content-Type" to "application/json")
                        body = """{"message": "PUT request received"}"""
                    }
                }
                "PATCH" -> {
                    // Handle PATCH requests
                    val id = input.pathParameters?.get("id")
                    if (id != null) {
                        if (input.body != null && input.body.isNotEmpty()) {
                            val statusUpdate = gson.fromJson(input.body, PunchData::class.java)
                            val updatedId = dynamoService.updatePunchStatus(id, statusUpdate.status)
                            return APIGatewayProxyResponseEvent().apply {
                                statusCode = 200
                                headers = mapOf("Content-Type" to "application/json")
                                body = """{"message": "Punch status updated successfully", "id": "$updatedId"}"""
                            }
                        } else {
                            return APIGatewayProxyResponseEvent().apply {
                                statusCode = 400
                                headers = mapOf("Content-Type" to "application/json")
                                body = """{"message": "Invalid request: No status provided in body"}"""
                            }
                        }
                    } else {
                        return APIGatewayProxyResponseEvent().apply {
                            statusCode = 400
                            headers = mapOf("Content-Type" to "application/json")
                            body = """{"message": "Invalid request: ID is missing"}"""
                        }
                    }
                }
                else -> {
                    return APIGatewayProxyResponseEvent().apply {
                        statusCode = 400
                        headers = mapOf("Content-Type" to "application/json")
                        body = """{"message": "Invalid HTTP method"}"""
                    }
                }
            }
        } catch (e: Exception) {
            logger.log("ERROR: ${e.message}")
            return APIGatewayProxyResponseEvent().apply {
                statusCode = 500
                headers = mapOf("Content-Type" to "application/json")
                body = """{"message": "Failed to fetch data"}"""
            }
        }
    }
}


