package com.cleanwin.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cleanwin.app.MainActivity
import com.cleanwin.app.R
import com.cleanwin.app.data.local.AppDatabase
import com.cleanwin.app.data.local.SecurityManager
import com.cleanwin.app.data.local.entity.MessageEntity
import com.cleanwin.app.data.local.entity.RuleEntity
import com.cleanwin.app.data.remote.DeepSeekRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NotificationMonitorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    private lateinit var securityManager: SecurityManager
    private lateinit var deepSeekRepository: DeepSeekRepository

    private val recentNotifications = mutableMapOf<Int, Long>()
    private val DEBOUNCE_WINDOW_MS = 3000L

    private val blacklistedPackages = mutableSetOf(
        "android",
        "com.android.systemui",
        "com.netease.cloudmusic",
        "com.cleanwin.app"
    )

    // 内存缓存：存储 (规则名 + 发送者) 对应的消息列表
    // Key: category + title
    private val messageStacks = ConcurrentHashMap<String, MutableList<String>>()

    companion object {
        private const val CHANNEL_ID = "clean_win_service"
        private const val DELIVERY_CHANNEL_ID = "clean_win_delivery"
        private const val ROOT_GROUP_KEY = "com.cleanwin.app.TOTAL_COLLECTION"

        fun triggerTaskEndDelivery(context: Context) {
            val db = AppDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                val undelivered = db.messageDao().getUndeliveredMessagesByRuleType(1)
                undelivered.forEach { msg ->
                    showInAppNotification(context, msg)
                    db.messageDao().updateMessage(msg.copy(isDelivered = true))
                }
            }
        }

        fun showInAppNotification(context: Context, msg: MessageEntity) {
            val sm = SecurityManager(context)
            if (!sm.isNotificationsEnabled()) return // 如果关闭了通知开关，则不弹出

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val originalIntent = msg.notificationKey?.let { IntentManager.getIntent(it) }
            
            val service = context as? NotificationMonitorService
            
            val stackKey = msg.category + "_" + msg.title
            
            val actualContent = msg.aiResult ?: msg.content
            
            val currentList = service?.messageStacks?.get(stackKey) ?: mutableListOf()
            
            // 防止重复添加完全相同的最新消息导致列表冗余
            if (currentList.isEmpty() || currentList.last() != actualContent) {
                currentList.add(actualContent)
            }
            service?.messageStacks?.put(stackKey, currentList)

            // 构建带有序号和换行的文本内容，明确展示列表
            val displayContent = currentList.joinToString(separator = "\n") { "- " + it }

            val notificationBuilder = NotificationCompat.Builder(context, DELIVERY_CHANNEL_ID)
                .setContentTitle("[" + msg.category + "] " + msg.title)
                .setContentText(currentList.last()) // 收起时显示最后一条
                .setStyle(NotificationCompat.BigTextStyle().bigText(displayContent)) // 展开时显示列表
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setGroup(ROOT_GROUP_KEY) 
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true) 
                .setAutoCancel(true)

            if (originalIntent != null) {
                notificationBuilder.setContentIntent(originalIntent)
            }

            val summaryNotification = NotificationCompat.Builder(context, DELIVERY_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.intercept_center_title))
                .setContentText(context.getString(R.string.intercept_center_content))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setGroup(ROOT_GROUP_KEY)
                .setGroupSummary(true) 
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .build()

            notificationManager.notify(stackKey.hashCode(), notificationBuilder.build())
            notificationManager.notify(ROOT_GROUP_KEY.hashCode(), summaryNotification)
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        securityManager = SecurityManager(applicationContext)
        deepSeekRepository = DeepSeekRepository { securityManager.getApiKey() }
        blacklistedPackages.add(packageName)
        createNotificationChannels()
        startForegroundService()
        startScheduledCheck()
    }

    private fun startScheduledCheck() {
        serviceScope.launch {
            while (isActive) {
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val rules = database.messageDao().getAllRules()
                val scheduledRules = rules.filter { it.deliveryType == 2 && it.scheduledTime == currentTime }
                for (rule in scheduledRules) {
                    val messages = database.messageDao().getUndeliveredMessagesByRuleType(2)
                    messages.filter { it.category == rule.category }.forEach { msg ->
                        showInAppNotification(this@NotificationMonitorService, msg)
                        database.messageDao().updateMessage(msg.copy(isDelivered = true))
                    }
                }
                delay(60000) 
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val serviceChannel = NotificationChannel(CHANNEL_ID, getString(R.string.service_status_channel), NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
            }
            manager.createNotificationChannel(serviceChannel)
            val deliveryChannel = NotificationChannel(DELIVERY_CHANNEL_ID, getString(R.string.delivery_channel), NotificationManager.IMPORTANCE_HIGH).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(deliveryChannel)
        }
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_ready_title))
            .setContentText(getString(R.string.service_ready_content))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (!securityManager.isServiceEnabled()) return
        if (sbn == null) return
        val packageName = sbn.packageName
        if (blacklistedPackages.contains(packageName) || packageName == this.packageName) return
        val notification = sbn.notification
        val title = notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        if (title.isBlank() && text.isBlank()) return
        val contentHash = "${packageName}|${title}|${text}".hashCode()
        val currentTime = System.currentTimeMillis()
        synchronized(recentNotifications) {
            val lastSeen = recentNotifications[contentHash]
            if (lastSeen != null && (currentTime - lastSeen) < DEBOUNCE_WINDOW_MS) return
            recentNotifications[contentHash] = currentTime
        }
        serviceScope.launch {
            try {
                notification.contentIntent?.let { IntentManager.saveIntent(sbn.key, it) }
                
                // 获取应用显示名称
                val appLabel = try {
                    val pm = packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName.split(".").lastOrNull() ?: packageName
                }

                val rules = database.messageDao().getAllRules()
                var matchedRule: RuleEntity? = null
                var matchedKwd: String? = null
                for (rule in rules) {
                    val keywords = rule.getKeywordList()
                    val matchedKeyword = keywords.find { keyword ->
                        when (rule.matchType) {
                            0 -> title.contains(keyword)
                            1 -> text.contains(keyword)
                            2 -> appLabel == keyword // 软件特权：匹配应用名
                            else -> false
                        }
                    }
                    if (matchedKeyword != null) {
                        matchedRule = rule
                        matchedKwd = matchedKeyword
                        break
                    }
                }
                    val entity = MessageEntity(
                        packageName = packageName,
                        title = title,
                        content = text,
                        timestamp = currentTime,
                        category = matchedRule?.category ?: getString(R.string.category_unclassified),
                        notificationKey = sbn.key,
                        matchedKeyword = matchedKwd 
                    )
                val id = database.messageDao().insertMessage(entity)
                val savedEntity = entity.copy(id = id.toInt())
                if (matchedRule != null && matchedRule.deliveryType == 0) {
                     showInAppNotification(this@NotificationMonitorService, savedEntity)
                     database.messageDao().updateMessage(savedEntity.copy(isDelivered = true))
                }
                if (securityManager.getApiKey().isNotBlank() && matchedRule != null) {
                    performAiAnalysis(savedEntity)
                }
            } catch (e: Exception) {
                Log.e("CleanWinService", "Error", e)
            }
        }
    }

    private fun performAiAnalysis(entity: MessageEntity) {
        serviceScope.launch {
            try {
                val aiTask = deepSeekRepository.extractTaskFromNotification(entity.title, entity.content)       
                if (!aiTask.isNullOrBlank() && aiTask != "无") {
                    val updatedEntity = entity.copy(isAnalyzed = true, aiResult = aiTask)
                    database.messageDao().updateMessage(updatedEntity)
                    if (updatedEntity.isDelivered) {
                        // AI分析完成后更新内存列表中的最后一条消息
                        val stackKey = updatedEntity.category + "_" + updatedEntity.title
                        val currentList = messageStacks[stackKey]
                        if (currentList != null && currentList.isNotEmpty()) {
                            // 替换为 AI 结果
                            currentList[currentList.size - 1] = aiTask
                            messageStacks[stackKey] = currentList
                        }
                        showInAppNotification(this@NotificationMonitorService, updatedEntity)
                    }
                }
            } catch (e: Exception) {
                Log.e("CleanWinService", "AI Error", e)
            }
        }
    }
}



