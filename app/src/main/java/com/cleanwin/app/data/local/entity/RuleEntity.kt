package com.cleanwin.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keywords: String, // 用 ||| 分隔
    val category: String, // UI显示为规则名
    val matchType: Int, // 0:消息人名, 1:包含关键字
    val deliveryType: Int = 0, // 0: 立即, 1: 任务结束时, 2: 定时送达
    val scheduledTime: String? = null, // 格式 "HH:mm"
    val sortOrder: Int = 0 // 排序权重
) {
    fun getKeywordList(): List<String> {
        return if (keywords.isBlank()) emptyList() else keywords.split("|||")
    }
}
