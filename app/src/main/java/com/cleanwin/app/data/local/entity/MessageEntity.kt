package com.cleanwin.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String = "未分类",
    val isAnalyzed: Boolean = false,
    val aiResult: String? = null,
    val notificationKey: String? = null,
    val isDelivered: Boolean = false,
    val matchedKeyword: String? = null // 新增：记录匹配到的具体关键词
) {
    fun parseAiTask(): Pair<String, String> {
        val jsonString = aiResult ?: return "未知" to (aiResult ?: "")
        if (!jsonString.startsWith("{")) return "未知" to jsonString
        
        return try {
            val json = android.util.JsonReader(java.io.StringReader(jsonString))
            var time = "未知"
            var task = ""
            json.beginObject()
            while (json.hasNext()) {
                when (json.nextName()) {
                    "time" -> time = json.nextString()
                    "task" -> task = json.nextString()
                    else -> json.skipValue()
                }
            }
            json.endObject()
            json.close()
            time to task
        } catch (e: Exception) {
            "未知" to jsonString
        }
    }
}
