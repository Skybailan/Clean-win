package com.cleanwin.app.data.local.dao
import androidx.room.*
import com.cleanwin.app.data.local.entity.MessageEntity
import com.cleanwin.app.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isAnalyzed = 1 ORDER BY timestamp DESC")
    fun getAiProcessedTasksFlow(): Flow<List<MessageEntity>>

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity)
    
    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Update
    suspend fun updateRules(rules: List<RuleEntity>)

    @Query("SELECT * FROM rules ORDER BY sortOrder ASC")
    fun getAllRulesFlow(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules ORDER BY sortOrder ASC")
    suspend fun getAllRules(): List<RuleEntity>

    @Delete
    suspend fun deleteRule(rule: RuleEntity)

    @Query("""
        SELECT m.* FROM messages m 
        JOIN rules r ON m.category = r.category 
        WHERE m.isDelivered = 0 AND r.deliveryType = :deliveryType
    """)
    suspend fun getUndeliveredMessagesByRuleType(deliveryType: Int): List<MessageEntity>

    @Query("""
        SELECT m.* FROM messages m 
        JOIN rules r ON m.category = r.category 
        WHERE m.isDelivered = 0 AND r.deliveryType = :deliveryType
    """)
    fun getUndeliveredMessagesByRuleTypeFlow(deliveryType: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE category = :category ORDER BY timestamp DESC")
    fun getMessagesByCategoryFlow(category: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE category = :category")
    suspend fun deleteMessagesByCategory(category: String)
}

