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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

data class NodeState(
    val id: String,
    val name: String,
    val type: String,
    var offset: Offset,
    val metrics: Map<String, String>,
    val logs: List<String>
)

/**
 * Advanced Architectural Diagnostic Hub & Maintenance Workspace.
 * Full-screen, zoomable, and pannable workspace with "Water Flow" logic.
 */
@Composable
fun ArchitecturalDiagnosticHub(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Workspace State
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    var isSyncing by remember { mutableStateOf(false) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    
    // Live Pulse for "Water Flow"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow"
    )

    // Node Definitions
    val nodes = remember {
        mutableStateListOf(
            NodeState("DB", "SQLite Engine", "STORAGE", Offset(100f, 200f), mapOf("Integrity" to "100%", "Health" to "Optimal"), listOf("VACUUM OK", "Indices active")),
            NodeState("BRAIN", "Context Brain", "LOGIC", Offset(400f, 150f), mapOf("Aware" to "True", "Context" to "Hub"), listOf("State: PERSISTED")),
            NodeState("NLU", "NLU Orchestrator", "INTENT", Offset(700f, 250f), mapOf("Conf" to "98%", "Lat" to "12ms"), listOf("Pattern: LOCAL_DIALECT")),
            NodeState("TESTS", "JUnit Suite", "QUALITY", Offset(400f, 400f), mapOf("Passed" to "57/57", "Coverage" to "92%"), listOf("Suite: STABLE")),
            NodeState("UI", "Medical Hub", "PHYSICAL", Offset(1000f, 200f), mapOf("FPS" to "120", "Theme" to "Med"), listOf("Layer cache: HIT"))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Space Black
            .pointerInput(Unit) {
                detectTapGestures { selectedNodeId = null }
            }
    ) {
        // Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx() * scale
            val xOffset = offset.x % gridStep
            val yOffset = offset.y % gridStep
            
            for (x in 0..size.width.toInt() step gridStep.toInt()) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.3f), Offset(x.toFloat() + xOffset, 0f), Offset(x.toFloat() + xOffset, size.height))
            }
            for (y in 0..size.height.toInt() step gridStep.toInt()) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.3f), Offset(0f, y.toFloat() + yOffset), Offset(size.width, y.toFloat() + yOffset))
            }
        }

        // Transformable Workspace Hub
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            // Water Flow Connections
            Canvas(modifier = Modifier.fillMaxSize()) {
                nodes.forEach { startNode ->
                    // Simplified: Connect to next logic nodes
                    val connections = when(startNode.id) {
                        "DB" -> listOf("BRAIN")
                        "BRAIN" -> listOf("NLU", "TESTS")
                        "NLU" -> listOf("UI")
                        "TESTS" -> listOf("UI")
                        else -> emptyList()
                    }
                    
                    connections.forEach { targetId ->
                        val endNode = nodes.find { it.id == targetId }
                        if (endNode != null) {
                            val start = startNode.offset + Offset(70f, 40f)
                            val end = endNode.offset + Offset(0f, 40f)
                            
                            val path = Path().apply {
                                moveTo(start.x, start.y)
                                cubicTo(
                                    x1 = start.x + 100, y1 = start.y,
                                    x2 = end.x - 100, y2 = end.y,
                                    x3 = end.x, y3 = end.y
                                )
                            }
                            
                            // Base Connection
                            drawPath(
                                path = path,
                                color = Color(0xFF1E293B),
                                style = Stroke(width = 2f)
                            )
                            
                            // Water Flow Animation
                            if (isSyncing) {
                                val pathMeasure = android.graphics.PathMeasure(path.asAndroidPath(), false)
                                val pos = floatArrayOf(0f, 0f)
                                pathMeasure.getPosTan(pathMeasure.length * flowProgress, pos, null)
                                
                                drawCircle(
                                    color = Color(0xFF0D9488),
                                    radius = 4f,
                                    center = Offset(pos[0], pos[1])
                                )
                            }
                        }
                    }
                }
            }

            // Workspace Nodes
            nodes.forEach { node ->
                MaintenanceNode(
                    node = node,
                    isSelected = selectedNodeId == node.id,
                    onSelect = { selectedNodeId = node.id },
                    onDrag = { delta ->
                        node.offset += delta / scale
                    }
                )
            }
        }

        // Overlay Components (Non-Transformable)
        TopControls(
            isSyncing = isSyncing,
            onBack = onNavigateBack,
            onRun = {
                isSyncing = true
                scope.launch { delay(3000.milliseconds); isSyncing = false }
            }
        )

        // Bottom Maintenance Panel
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            MaintenancePanel(nodes.find { it.id == selectedNodeId })
        }
    }
}

@Composable
fun MaintenanceNode(
    node: NodeState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    val density = LocalDensity.current
    Surface(
        modifier = Modifier
            .offset { IntOffset(node.offset.x.roundToInt(), node.offset.y.roundToInt()) }
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
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                Spacer(Modifier.width(8.dp))
                Text(node.type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
            }
            Text(node.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TopControls(
    isSyncing: Boolean,
    onBack: () -> Unit,
    onRun: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.background(Color(0xFF0F172A), CircleShape)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        
        Button(
            onClick = onRun,
            enabled = !isSyncing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(if (isSyncing) Icons.Rounded.HourglassEmpty else Icons.Rounded.Memory, null)
            Spacer(Modifier.width(8.dp))
            Text(if (isSyncing) "RUNNING DIAGNOSTIC..." else "MAINTENANCE MODE")
        }
    }
}

@Composable
fun MaintenancePanel(selectedNode: NodeState?) {
    val scrollState = rememberLazyListState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(selectedNode?.name ?: "System Global", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Live Log Stream & Hub Monitoring", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
                
                if (selectedNode != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        selectedNode.metrics.forEach { (k, v) ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(k.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(v, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            
            // Console Terminal
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.2f))
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.padding(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item { Text("> AGENTIC_OS_INITIALIZED", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp) }
                    item { Text("> 120FPS_MODE_VERIFIED", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp) }
                    
                    if (selectedNode != null) {
                        items(selectedNode.logs) { log ->
                            Text("> TRACE: $log", fontFamily = FontFamily.Monospace, color = Color(0xFF22C55E), fontSize = 11.sp)
                        }
                    } else {
                        item { Text("> STANDBY: SELECT NODE FOR TRACE", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8), fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}
