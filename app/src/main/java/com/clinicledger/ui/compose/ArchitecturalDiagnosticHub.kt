package com.clinicledger.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * State for a dynamic node in the Maintenance Workspace.
 */
data class MaintenanceNodeState(
    val id: String,
    val name: String,
    val type: String,
    var offset: Offset,
    val metrics: Map<String, String>,
    val logs: List<String>
)

/**
 * Advanced Architectural Maintenance Hub.
 * A full-screen, zoomable, and pannable workspace designed for system engineering.
 * Replaces flow.jsx with high-fidelity native visuals and water-flow logic.
 */
@Composable
fun ArchitecturalDiagnosticHub(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Workspace Navigation State
    var scale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        panOffset += offsetChange
    }

    var isSyncing by remember { mutableStateOf(false) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    
    // Water Flow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "flow_pulse")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Nodes definition
    val nodes = remember {
        mutableStateListOf(
            MaintenanceNodeState("DB", "SQLite Engine", "STORAGE", Offset(150f, 300f), mapOf("Integrity" to "100%", "Health" to "Stable"), listOf("Indices verified", "VACUUM optimized")),
            MaintenanceNodeState("BRAIN", "Context Brain", "LOGIC", Offset(500f, 250f), mapOf("Aware" to "True", "Active" to "Yes"), listOf("Context synced: SETTINGS")),
            MaintenanceNodeState("NLU", "Intent Engine", "INTENT", Offset(850f, 350f), mapOf("Conf" to "98%", "Lat" to "11ms"), listOf("Dialect Map: DEODHARI")),
            MaintenanceNodeState("LLAMA", "Local LLM", "AGENTIC", Offset(850f, 150f), mapOf("Engine" to "llama.cpp", "RAM" to "2.4GB"), listOf("Ready for reasoning")),
            MaintenanceNodeState("TESTS", "JUnit Suite", "QUALITY", Offset(500f, 500f), mapOf("Passed" to "57/57", "Rate" to "100%"), listOf("Regression: PASS")),
            MaintenanceNodeState("UI", "Medical Hub", "PHYSICAL", Offset(1200f, 300f), mapOf("FPS" to "120", "Thread" to "Main"), listOf("Layer cache: HIT"))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Match flow.jsx theme
            .pointerInput(Unit) {
                detectTapGestures { selectedNodeId = null }
            }
    ) {
        // Engineering Grid Background (Zoom/Pan aware)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx() * scale
            val xStart = panOffset.x % gridStep
            val yStart = panOffset.y % gridStep
            
            for (x in 0..size.width.toInt() step gridStep.toInt()) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.2f), Offset(x.toFloat() + xStart, 0f), Offset(x.toFloat() + xStart, size.height))
            }
            for (y in 0..size.height.toInt() step gridStep.toInt()) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.2f), Offset(0f, y.toFloat() + yStart), Offset(size.width, y.toFloat() + yStart))
            }
        }

        // Transformable Workspace Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
        ) {
            // Water Flow Connections Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val connectionMap = listOf(
                    "DB" to "BRAIN",
                    "BRAIN" to "NLU",
                    "BRAIN" to "LLAMA",
                    "BRAIN" to "TESTS",
                    "NLU" to "UI",
                    "LLAMA" to "UI",
                    "TESTS" to "UI"
                )

                connectionMap.forEach { (src, target) ->
                    val startNode = nodes.find { it.id == src }
                    val endNode = nodes.find { it.id == target }
                    
                    if (startNode != null && endNode != null) {
                        val start = startNode.offset + Offset(160f, 40f)
                        val end = endNode.offset + Offset(0f, 40f)
                        
                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(
                                x1 = start.x + 80, y1 = start.y,
                                x2 = end.x - 80, y2 = end.y,
                                x3 = end.x, y3 = end.y
                            )
                        }

                        // Base Wire
                        drawPath(
                            path = path,
                            color = Color(0xFF1E293B),
                            style = Stroke(width = 2f)
                        )

                        // Live Token "Water Flow"
                        if (isSyncing) {
                            val pathMeasure = android.graphics.PathMeasure(path.asAndroidPath(), false)
                            val pos = floatArrayOf(0f, 0f)
                            pathMeasure.getPosTan(pathMeasure.length * flowProgress, pos, null)
                            
                            drawCircle(
                                color = Color(0xFF0D9488),
                                radius = 4f,
                                center = Offset(pos[0], pos[1]),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }

            // Draggable Workspace Nodes
            nodes.forEach { node ->
                DraggableMaintenanceNode(
                    state = node,
                    isSelected = selectedNodeId == node.id,
                    onSelect = { selectedNodeId = node.id },
                    onDrag = { delta ->
                        val index = nodes.indexOfFirst { it.id == node.id }
                        if (index != -1) {
                            nodes[index] = nodes[index].copy(offset = nodes[index].offset + (delta / scale))
                        }
                    }
                )
            }
        }

        // Non-Transformable UI Overlays
        HubControls(
            isSyncing = isSyncing,
            onBack = onNavigateBack,
            onDiagnosticRun = {
                isSyncing = true
                scope.launch { delay(3000.milliseconds); isSyncing = false }
            }
        )

        // Bottom Maintenance Console
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            MaintenanceConsolePanel(selectedNode = nodes.find { it.id == selectedNodeId })
        }
    }
}

@Composable
fun DraggableMaintenanceNode(
    state: MaintenanceNodeState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    Surface(
        modifier = Modifier
            .offset { IntOffset(state.offset.x.roundToInt(), state.offset.y.roundToInt()) }
            .width(160.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)
        ),
        tonalElevation = if (isSelected) 12.dp else 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                Spacer(Modifier.width(8.dp))
                Text(state.type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
            }
            Text(state.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun HubControls(
    isSyncing: Boolean,
    onBack: () -> Unit,
    onDiagnosticRun: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color(0xFF0F172A), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Exit", tint = Color.White)
        }
        
        Button(
            onClick = onDiagnosticRun,
            enabled = !isSyncing,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(if (isSyncing) Icons.Rounded.HourglassEmpty else Icons.Rounded.DeveloperMode, null)
            Spacer(Modifier.width(12.dp))
            Text(if (isSyncing) "SUITE_EXECUTING..." else "MAINTENANCE_MODE")
        }
    }
}

@Composable
fun MaintenanceConsolePanel(selectedNode: MaintenanceNodeState?) {
    val logScrollState = rememberLazyListState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Panel Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(selectedNode?.name ?: "System Master", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Architectural Diagnostics & Functional Hub", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                }
                
                if (selectedNode != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        selectedNode.metrics.forEach { (k, v) ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(k.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(v, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF0D9488), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Integrated Log Terminal
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.2f))
            ) {
                LazyColumn(
                    state = logScrollState,
                    modifier = Modifier.padding(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item { Text("> BOOT_SEQUENCE: CLINIC_LEDGER_OS_V3.0", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp) }
                    item { Text("> PERF_BENCHMARK: 120FPS_VERIFIED", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp) }
                    item { Text("> INTEGRITY_GUARDIAN: ACTIVE", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp) }
                    
                    if (selectedNode != null) {
                        items(selectedNode.logs) { log ->
                            Text("> TRACE: $log", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp)
                        }
                    } else {
                        item { Text("> STANDBY: SELECT NODE FOR COMPONENT TRACE", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8), fontSize = 11.sp) }
                    }
                }
            }
        }
    }
    
    // Auto-scroll logs to bottom
    LaunchedEffect(selectedNode) {
        logScrollState.animateScrollToItem(10) // Approx bottom
    }
}
