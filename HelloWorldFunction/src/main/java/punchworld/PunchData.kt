package punchworld

data class PunchData(
        val createdBy: String,
        val createdAt: String,
        val id: String,
        val imgUrl: List<String>,
        val modifiedBy: String,
        val modifiedAt: String,
        val summary: String,
        val status : String
    )