package com.cleanwin.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleanwin.app.data.local.AppDatabase
import com.cleanwin.app.data.local.SecurityManager
import com.cleanwin.app.data.local.entity.MessageEntity
import com.cleanwin.app.data.local.entity.RuleEntity
import com.cleanwin.app.service.NotificationMonitorService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.messageDao()
    private val securityManager = SecurityManager(application)

    val messages: StateFlow<List<MessageEntity>> = dao.getAllMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<RuleEntity>> = dao.getAllRulesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _apiKey = MutableStateFlow(securityManager.getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isServiceEnabled = MutableStateFlow(securityManager.isServiceEnabled())
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(securityManager.isNotificationsEnabled())
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    fun toggleService(enabled: Boolean) {
        securityManager.setServiceEnabled(enabled)
        _isServiceEnabled.value = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        securityManager.setNotificationsEnabled(enabled)
        _isNotificationsEnabled.value = enabled
    }

    fun saveApiKey(key: String) {
        securityManager.saveApiKey(key)
        _apiKey.value = key
    }

    fun addRule(keywords: String, category: String, matchType: Int, deliveryType: Int, scheduledTime: String?) {
        viewModelScope.launch {
            val currentRules = dao.getAllRules()
            val newSortOrder = if (currentRules.isEmpty()) 0 else currentRules.maxOf { it.sortOrder } + 1
            dao.insertRule(RuleEntity(
                keywords = keywords,
                category = category,
                matchType = matchType,
                deliveryType = deliveryType,
                scheduledTime = scheduledTime,
                sortOrder = newSortOrder
            ))
        }
    }

    fun updateRule(rule: RuleEntity) {
        viewModelScope.launch {
            dao.updateRule(rule)
        }
    }

    fun deleteRule(rule: RuleEntity) {
        viewModelScope.launch {
            dao.deleteRule(rule)
        }
    }

    fun moveRule(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val currentRules = rules.value.toMutableList()
            if (fromIndex in currentRules.indices && toIndex in currentRules.indices) {
                val movedRule = currentRules.removeAt(fromIndex)
                currentRules.add(toIndex, movedRule)

                val updatedRules = currentRules.mapIndexed { index, rule ->
                    rule.copy(sortOrder = index)
                }

                dao.updateRules(updatedRules)
            }
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            dao.deleteMessage(message)
        }
    }

    fun deleteMessages(messagesToDelete: List<MessageEntity>) {
        viewModelScope.launch {
            messagesToDelete.forEach { dao.deleteMessage(it) }
        }
    }

    fun deleteMessagesByRule(category: String) {
        viewModelScope.launch {
            dao.deleteMessagesByCategory(category)
        }
    }

    // 新增：任务是否完成的状态
    private val _isTaskCompleted = MutableStateFlow(false)
    val isTaskCompleted: StateFlow<Boolean> = _isTaskCompleted.asStateFlow()

    // 新增：任务结束时送达的消息流
    val taskEndMessages: StateFlow<List<MessageEntity>> = dao.getUndeliveredMessagesByRuleTypeFlow(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTaskCompleted(completed: Boolean) {
        _isTaskCompleted.value = completed
        if (completed) {
            triggerTaskComplete()
        }
    }

    fun getMessagesForRule(category: String): Flow<List<MessageEntity>> {
        return dao.getMessagesByCategoryFlow(category)
    }

    fun triggerTaskComplete() {
        NotificationMonitorService.triggerTaskEndDelivery(getApplication())
    }
}

