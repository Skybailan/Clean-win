package com.cleanwin.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleanwin.app.R
import com.cleanwin.app.data.local.entity.MessageEntity
import com.cleanwin.app.data.local.entity.RuleEntity
import com.cleanwin.app.ui.viewmodel.MainViewModel
import com.cleanwin.app.ui.theme.*
import com.cleanwin.app.service.IntentManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// --- 商业级 UI 辅助组件 ---

@Composable
fun PremiumBackground(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val gradient = if (isDark) {
        Brush.verticalGradient(listOf(Brand1, Brand2, Brand0))
    } else {
        Brush.verticalGradient(listOf(Brand10, Brand11, Brand9))
    }
    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    // 使用完全不透明的纯色背景
    val bgColor = if (isDark) Brand2 else Brand10
    val borderColor = if (isDark) Brand3 else Brand8
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 增加阴影以体现层级
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val savedApiKey by viewModel.apiKey.collectAsState()
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val navItems = listOf(
        Triple(stringResource(R.string.nav_home), Icons.Rounded.Dashboard, Brand2),
        Triple(stringResource(R.string.nav_rules), Icons.Rounded.Shield, Brand3),
        Triple(stringResource(R.string.nav_overview), Icons.Rounded.AllInclusive, Brand4),
        Triple(stringResource(R.string.nav_analysis), Icons.Rounded.AutoAwesome, Brand5)
    )

    var showAddRuleDialog by remember { mutableStateOf(false) }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .shadow(20.dp, shape = CircleShape, ambientColor = Brand2),
                    // 底部导航栏改为完全不透明
                    color = if (isSystemInDarkTheme()) Brand1 else Color.White,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSystemInDarkTheme()) Brand3 else Brand9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = pagerState.currentPage == index
                            val contentColor by animateColorAsState(if (isSelected) Brand8 else Brand6, label = "navColor")
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { 
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) } 
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(48.dp) // 变窄一点点的交互背景
                                        .clip(CircleShape)
                                        .background(if (isSelected) Brand2 else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        item.second,
                                        contentDescription = item.first,
                                        modifier = Modifier.size(if (isSelected) 24.dp else 22.dp),
                                        tint = contentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    item.first, 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = pagerState.currentPage == 1,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { showAddRuleDialog = true },
                        shape = CircleShape,
                        containerColor = Brand2,
                        contentColor = Brand10,
                        modifier = Modifier.padding(bottom = 56.dp) // 向下挪动一些
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_rule))
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        0 -> HomePage(viewModel, rules)
                        1 -> RulesHomePage(viewModel, rules.sortedBy { it.sortOrder })
                        2 -> AppOverviewPage(messages, viewModel)
                        3 -> AiDashboardPage(messages, savedApiKey, onSaveKey = { viewModel.saveApiKey(it) }, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AddRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { keywords, category, matchType, deliveryType, scheduledTime ->
                viewModel.addRule(keywords, category, matchType, deliveryType, scheduledTime)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun HomePage(viewModel: MainViewModel, rules: List<RuleEntity>) {
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()
    val isTaskCompleted by viewModel.isTaskCompleted.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val context = LocalContext.current
    val packageManager = remember { context.packageManager }
    val appNameMap = remember(messages) {
        messages.map { it.packageName }.distinct().associateWith { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg.split(".").lastOrNull() ?: pkg
            }
        }
    }

    val parsedAiTasks = remember(messages) {
        messages.filter { it.isAnalyzed && it.aiResult != null }.map { msg ->
            val (time, task) = msg.parseAiTask()
            Triple(msg, time, task)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 头部状态卡片
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isServiceEnabled) stringResource(R.string.status_managing) else stringResource(R.string.status_stopped),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isServiceEnabled) Brand8 else Brand6
                        )
                        Text(
                            text = stringResource(R.string.status_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Brand7
                        )
                    }
                    
                    Switch(
                        checked = isServiceEnabled,
                        onCheckedChange = { viewModel.toggleService(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Brand10,
                            checkedTrackColor = Brand2,
                            uncheckedThumbColor = Brand6,
                            uncheckedTrackColor = Brand1
                        )
                    )
                }
            }
        }

        item {
            // 任务核心控制按钮
            val btnColor by animateColorAsState(if (isTaskCompleted) Brand3 else Brand2, label = "btnColor")
            Button(
                onClick = { viewModel.setTaskCompleted(!isTaskCompleted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, shape = MaterialTheme.shapes.extraLarge),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(containerColor = btnColor)
            ) {
                Icon(if (isTaskCompleted) Icons.Rounded.PlayArrow else Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isTaskCompleted) stringResource(R.string.btn_resume_task) else stringResource(R.string.btn_complete_task),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isTaskCompleted) {
            val activeTaskEndRules = rules.filter { rule ->
                rule.deliveryType == 1 && messages.any { it.category == rule.category }
            }

            if (activeTaskEndRules.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.header_cached_messages),
                        style = MaterialTheme.typography.labelLarge,
                        color = Brand7,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                }

                items(activeTaskEndRules) { rule ->
                    SwipeToDeleteContainer(onDelete = { 
                        // 在主页左滑规则：批量删除该规则当前所有拦截记录
                        // 这里通过 viewModel 触发删除逻辑
                        viewModel.deleteMessagesByRule(rule.category)
                    }) {
                        RuleItem(viewModel, rule)
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.header_ai_tasks),
                    style = MaterialTheme.typography.labelLarge,
                    color = Brand7,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }

            taskListContent(
                parsedTasks = parsedAiTasks,
                appNameMap = appNameMap,
                viewModel = viewModel
            )
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.taskListContent(
    parsedTasks: List<Triple<MessageEntity, String, String>>,
    appNameMap: Map<String, String>,
    viewModel: MainViewModel
) {
    if (parsedTasks.isEmpty()) {
        item {
            GlassCard {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_ai_tasks), color = Brand6)
                }
            }
        }
    } else {
        val unknownTasks = parsedTasks.filter { it.second == "未知" }
        val knownTasks = parsedTasks.filter { it.second != "未知" }.sortedBy { it.second }

        if (unknownTasks.isNotEmpty()) {
            item {
                var isUnknownExpanded by remember { mutableStateOf(false) }
                GlassCard(onClick = { isUnknownExpanded = !isUnknownExpanded }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, tint = Brand6)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.unknown_time_tasks, unknownTasks.size), style = MaterialTheme.typography.titleSmall)
                        }
                        Icon(
                            if (isUnknownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Brand6
                        )
                    }
                    AnimatedVisibility(visible = isUnknownExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            unknownTasks.forEach { taskData ->
                                SwipeToDeleteContainer(onDelete = { viewModel.deleteMessage(taskData.first) }) {
                                    AiTaskItem(taskData, appNameMap[taskData.first.packageName] ?: taskData.first.packageName)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        items(knownTasks) { taskData ->
            SwipeToDeleteContainer(onDelete = { viewModel.deleteMessage(taskData.first) }) {
                AiTaskItem(taskData, appNameMap[taskData.first.packageName] ?: taskData.first.packageName)
            }
        }
    }
}

@Composable
fun AddRuleDialog(
    rule: RuleEntity? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Int, Int, String?) -> Unit
) {
    val initialKeywords = remember { mutableStateListOf(*rule?.getKeywordList()?.toTypedArray() ?: arrayOf("")) }
    var category by remember { mutableStateOf(rule?.category ?: "") }
    var matchType by remember { mutableIntStateOf(rule?.matchType ?: 0) }
    var deliveryType by remember { mutableIntStateOf(rule?.deliveryType ?: 0) }
    var scheduledTime by remember { mutableStateOf(rule?.scheduledTime ?: "18:00") }
    
    var expandedMatch by remember { mutableStateOf(false) }
    var expandedDelivery by remember { mutableStateOf(false) }
    
    val matchTypeOptions = listOf(
        stringResource(R.string.match_contact),
        stringResource(R.string.match_keyword),
        stringResource(R.string.match_app_name)
    )
    val deliveryTypeOptions = listOf(
        stringResource(R.string.option_immediate),
        stringResource(R.string.option_task_end),
        stringResource(R.string.option_scheduled)
    )

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxHeight(0.85f)) {
            Text(
                if (rule == null) stringResource(R.string.dialog_new_rule) else stringResource(R.string.dialog_edit_rule),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Brand2
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(stringResource(R.string.label_rule_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.label_keyword_list), style = MaterialTheme.typography.labelLarge, color = Brand6)
                }

                itemsIndexed(initialKeywords) { index, keyword ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { initialKeywords[index] = it },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            placeholder = { Text(stringResource(R.string.hint_input_content)) }
                        )
                        if (initialKeywords.size > 1) {
                            IconButton(onClick = { initialKeywords.removeAt(index) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { initialKeywords.add("") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(stringResource(R.string.btn_add_more_keywords))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // 匹配模式：垂直排列
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(stringResource(R.string.label_match_mode), style = MaterialTheme.typography.labelLarge, color = Brand6)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedMatch = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(matchTypeOptions[matchType], overflow = TextOverflow.Ellipsis, maxLines = 1)
                            }
                            DropdownMenu(expanded = expandedMatch, onDismissRequest = { expandedMatch = false }) {
                                matchTypeOptions.forEachIndexed { index, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { matchType = index; expandedMatch = false })
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 送达时间：垂直排列
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(stringResource(R.string.label_delivery_time), style = MaterialTheme.typography.labelLarge, color = Brand6)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedDelivery = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(deliveryTypeOptions[deliveryType], overflow = TextOverflow.Ellipsis, maxLines = 1)
                            }
                            DropdownMenu(expanded = expandedDelivery, onDismissRequest = { expandedDelivery = false }) {
                                deliveryTypeOptions.forEachIndexed { index, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { deliveryType = index; expandedDelivery = false })
                                }
                            }
                        }
                    }

                    if (deliveryType == 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scheduledTime,
                            onValueChange = { scheduledTime = it },
                            label = { Text(stringResource(R.string.label_time_format)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { 
                        val finalKeywords = initialKeywords.filter { it.isNotBlank() }.joinToString("|||")
                        if (finalKeywords.isNotBlank() && category.isNotBlank()) {
                            onConfirm(finalKeywords, category, matchType, deliveryType, if(deliveryType==2) scheduledTime else null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand2)
                ) {
                    Text(stringResource(R.string.btn_save_rule))
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    content: @Composable () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    val offset by animateDpAsState(targetValue = if (isRevealed) (-80).dp else 0.dp, label = "swipe")
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Brand2 else Brand10

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -15 && !isRevealed) isRevealed = true
                    if (dragAmount > 15 && isRevealed) isRevealed = false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor) // 统一背景色，移除深色背景
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = { 
                onDelete()
                isRevealed = false
            }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = if (isDark) Brand8 else Brand4)
            }
        }

        Box(
            modifier = Modifier
                .offset(x = offset)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RulesHomePage(viewModel: MainViewModel, rules: List<RuleEntity>) {
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var targetDropIndex by remember { mutableStateOf<Int?>(null) }
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_rules_added), color = Brand6, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { 
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.rules_management), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Brand2)
                            Text(stringResource(R.string.rules_guide), style = MaterialTheme.typography.bodySmall, color = Brand6)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isNotificationsEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = if (isNotificationsEnabled) Brand3 else Brand6,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isNotificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Brand10,
                                    checkedTrackColor = Brand2,
                                    uncheckedThumbColor = Brand6,
                                    uncheckedTrackColor = Brand1
                                ),
                                modifier = Modifier.scale(0.8f) // 缩放一点让它更精致
                            )
                        }
                    }
                }
                
                itemsIndexed(rules, key = { _, r -> r.id }) { index, rule ->
                    val isDragging = draggedItemIndex == index
                    val zIndex = if (isDragging) 1f else 0f
                    val offsetY = if (isDragging) dragOffsetY.roundToInt() else 0

                    Column {
                        if (targetDropIndex == index && draggedItemIndex != null && draggedItemIndex != index) {
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Brand8))
                        }

                        SwipeToDeleteContainer(onDelete = { viewModel.deleteRule(rule) }) {
                            RuleItem(
                                viewModel = viewModel, 
                                rule = rule,
                                modifier = Modifier
                                    .zIndex(zIndex)
                                    .offset { IntOffset(0, offsetY) }
                                    .pointerInput(rules) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { 
                                                draggedItemIndex = index 
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                
                                                val itemHeight = 110.dp.toPx() 
                                                val relativeIndexOffset = (dragOffsetY / itemHeight).roundToInt()
                                                val newIndex = (index + relativeIndexOffset).coerceIn(0, rules.size - 1)
                                                targetDropIndex = newIndex
                                            },
                                            onDragEnd = {
                                                targetDropIndex?.let { toIndex ->
                                                    if (toIndex != index) {
                                                        viewModel.moveRule(index, toIndex)
                                                    }
                                                }
                                                draggedItemIndex = null
                                                dragOffsetY = 0f
                                                targetDropIndex = null
                                            },
                                            onDragCancel = {
                                                draggedItemIndex = null
                                                dragOffsetY = 0f
                                                targetDropIndex = null
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RuleItem(viewModel: MainViewModel, rule: RuleEntity, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val capturedMessages by viewModel.getMessagesForRule(rule.category).collectAsState(initial = emptyList())

    val deliveryHint = when (rule.deliveryType) {
        1 -> stringResource(R.string.delivery_task_end)
        2 -> stringResource(R.string.delivery_scheduled, rule.scheduledTime ?: "")
        else -> stringResource(R.string.delivery_immediate)
    }
    
    val matchModeHint = when (rule.matchType) {
        0 -> stringResource(R.string.match_contact)
        2 -> stringResource(R.string.match_app)
        else -> stringResource(R.string.match_keyword)
    }

    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isSystemInDarkTheme()) Brand8 else Brand2, // 黑夜模式使用高对比色
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp).offset(y = 2.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(rule.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "$matchModeHint | $deliveryHint",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brand6
                )
            }
            
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(20.dp), tint = Brand5)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Brand5
                )
            }
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                HorizontalDivider(color = Brand8, thickness = 0.5.dp) // 使用实色分割线
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    stringResource(R.string.label_keywords, rule.getKeywordList().joinToString("、")),
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (capturedMessages.isEmpty()) {
                    Text(stringResource(R.string.no_intercept_records), style = MaterialTheme.typography.bodySmall, color = Brand7)
                } else {
                    capturedMessages.groupBy { it.matchedKeyword ?: "其他" }.forEach { (keyword, msgs) ->
                        if (msgs.size > 1) {
                            SwipeToDeleteContainer(onDelete = { viewModel.deleteMessages(msgs) }, shape = MaterialTheme.shapes.large) {
                                SubCollectionItem(viewModel, keyword, msgs)
                            }
                        } else {
                            val msg = msgs.first()
                            SwipeToDeleteContainer(onDelete = { viewModel.deleteMessage(msg) }, shape = RoundedCornerShape(8.dp)) {
                                SingleMessageItem(viewModel, msg)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AddRuleDialog(
            rule = rule,
            onDismiss = { showEditDialog = false },
            onConfirm = { keywords, category, matchType, deliveryType, scheduledTime ->
                viewModel.updateRule(rule.copy(
                    keywords = keywords,
                    category = category,
                    matchType = matchType,
                    deliveryType = deliveryType,
                    scheduledTime = scheduledTime
                ))
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SubCollectionItem(viewModel: MainViewModel, label: String, messages: List<MessageEntity>) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Surface(
            // 修改：黑夜模式下使用 Brand3 营造层次感，避免与背景色 Brand2 粘连
            color = if (isSystemInDarkTheme()) Brand3 else Brand10,
            shape = MaterialTheme.shapes.large,
            onClick = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = Brand3,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$label (${messages.size})", style = MaterialTheme.typography.labelLarge)
                }
                
                val latestKey = messages.first().notificationKey
                val isAvailable = latestKey != null && IntentManager.isIntentAvailable(latestKey)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            latestKey?.let { key ->
                                IntentManager.getIntent(key)?.send()
                                viewModel.deleteMessages(messages)
                            }
                        },
                        enabled = isAvailable
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward, // 使用带横线的箭头
                            contentDescription = stringResource(R.string.action_go_to),
                            modifier = Modifier.size(18.dp),
                            tint = if (isAvailable) Brand3 else Brand6
                        )
                    }
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = Brand6,
                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                    )
                }
            }
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth()) { // 移除 start = 16.dp 的边距
                messages.forEach { msg ->
                    SwipeToDeleteContainer(onDelete = { viewModel.deleteMessage(msg) }, shape = RoundedCornerShape(8.dp)) {
                        SingleMessageItem(viewModel, msg, showTitle = true)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleMessageItem(viewModel: MainViewModel, msg: MessageEntity, showTitle: Boolean = true) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Brand2 else Brand10
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        color = bgColor, // 使其不透明，避免侧滑时背景透出
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (showTitle) {
                    Text(text = msg.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Text(text = msg.content, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            
            val isAvailable = msg.notificationKey?.let { IntentManager.isIntentAvailable(it) } ?: false
            IconButton(
                onClick = { 
                    msg.notificationKey?.let { key ->
                        IntentManager.getIntent(key)?.send()
                        viewModel.deleteMessage(msg) 
                    }
                },
                enabled = isAvailable
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward, // 使用带横线的箭头
                    contentDescription = stringResource(R.string.action_go_to),
                    modifier = Modifier.size(18.dp),
                    tint = if (isAvailable) Brand3 else Brand6
                )
            }
        }
    }
}

@Composable
fun AppOverviewPage(messages: List<MessageEntity>, viewModel: MainViewModel) {
    val context = LocalContext.current
    val packageManager = remember { context.packageManager }
    
    val appNameMap = remember(messages) {
        messages.map { it.packageName }.distinct().associateWith { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg.split(".").lastOrNull() ?: pkg
            }
        }
    }

    val filteredMessages = messages.filter { it.packageName != context.packageName }
    val groupedByApp = filteredMessages.groupBy { appNameMap[it.packageName] ?: it.packageName }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { 
            Text(stringResource(R.string.app_overview_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Brand2)
        }
        
        if (filteredMessages.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_app_messages), color = Brand6)
                }
            }
        } else {
            items(groupedByApp.keys.toList()) { appName ->
                val appMessages = groupedByApp[appName] ?: emptyList()
                SwipeToDeleteContainer(onDelete = { viewModel.deleteMessages(appMessages) }) {
                    LargeCollectionItem(viewModel, appName, appMessages)
                }
            }
        }
    }
}

@Composable
fun LargeCollectionItem(viewModel: MainViewModel, appName: String, messages: List<MessageEntity>) {
    var expanded by remember { mutableStateOf(false) }
    
    GlassCard(onClick = { expanded = !expanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Brand9,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(appName.take(1), fontWeight = FontWeight.Bold, color = Brand2)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.total_notifications, messages.size), style = MaterialTheme.typography.bodySmall, color = Brand6)
            }
            
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Brand6,
                modifier = Modifier.size(24.dp)
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                messages.groupBy { 
                    if (it.title.length >= 3) it.title.take(3) else it.content.take(3) 
                }.forEach { (prefix, subMsgs) ->
                    if (subMsgs.size > 1) {
                        SwipeToDeleteContainer(onDelete = { viewModel.deleteMessages(subMsgs) }, shape = MaterialTheme.shapes.large) {
                            SubCollectionItem(viewModel, prefix, subMsgs)
                        }
                    } else {
                        val msg = subMsgs.first()
                        SwipeToDeleteContainer(onDelete = { viewModel.deleteMessage(msg) }, shape = RoundedCornerShape(8.dp)) {
                            SingleMessageItem(viewModel, msg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiDashboardPage(messages: List<MessageEntity>, savedApiKey: String, onSaveKey: (String) -> Unit, viewModel: MainViewModel) {
    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    val context = LocalContext.current
    val packageManager = remember { context.packageManager }

    val appNameMap = remember(messages) {
        messages.map { it.packageName }.distinct().associateWith { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg.split(".").lastOrNull() ?: pkg
            }
        }
    }

    val parsedAiTasks = remember(messages) {
        messages.filter { it.isAnalyzed && it.aiResult != null }.map { msg ->
            val (time, task) = msg.parseAiTask()
            Triple(msg, time, task)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.ai_manager_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Brand2)
            Text(stringResource(R.string.ai_manager_subtitle), style = MaterialTheme.typography.bodySmall, color = Brand6)
        }

        item {
            GlassCard {
                Text(stringResource(R.string.api_config), style = MaterialTheme.typography.labelLarge, color = Brand2)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.weight(1f),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.hint_api_key)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSaveKey(apiKeyInput) },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Brand2)
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }

        if (savedApiKey.isBlank()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.api_key_required), textAlign = TextAlign.Center, color = Brand6)
                }
            }
        } else {
            taskListContent(
                parsedTasks = parsedAiTasks,
                appNameMap = appNameMap,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun AiTaskItem(taskData: Triple<MessageEntity, String, String>, appName: String) {
    val (msg, timeRaw, taskContent) = taskData
    val time = if (timeRaw == "未知") stringResource(R.string.unknown) else timeRaw
    val isDark = isSystemInDarkTheme()

    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                color = if (isDark) Brand3 else Brand2, // 适配黑夜模式
                shape = CircleShape,
                modifier = Modifier.size(32.dp).padding(top = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.EventNote, contentDescription = null, tint = Brand10, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.labelLarge, color = Brand2, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Brand9, shape = CircleShape) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Brand3
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(taskContent, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.label_source, msg.title), style = MaterialTheme.typography.bodySmall, color = Brand6)
            }

            val isAvailable = msg.notificationKey?.let { IntentManager.isIntentAvailable(it) } ?: false
            IconButton(
                onClick = {
                    msg.notificationKey?.let { key -> IntentManager.getIntent(key)?.send() }
                },
                enabled = isAvailable
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward, // 使用带横线的箭头 
                    contentDescription = stringResource(R.string.action_go_to),
                    modifier = Modifier.size(20.dp),
                    tint = if (isAvailable) Brand3 else Brand6
                )
            }
        }
    }
}
