package com.clinicledger.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * State representing a logical system component in the maintenance graph.
 */
data class MaintenanceNodeState(
    /** Unique tag */
    val id: String,
    /** Human name */
    val name: String,
    /** Layer type */
    val type: String,
    /** XY position in viewport */
    var offset: Offset,
    /** Key-value health metrics */
    val metrics: Map<String, String>,
    /** Recent component logs */
    val logs: List<String>,
)

/**
 * Immersive architectural visualizer and diagnostic tool.
 */
@Suppress("HardcodedStringLiteral")
@Composable
fun ArchitecturalDiagnosticHub(
    /** Exit callback */
    onNavigateBack: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    var isSyncing by remember { mutableStateOf(value = false) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    val nodes = remember {
        mutableStateListOf(
            MaintenanceNodeState(
                "DB", "SQLite Engine", "STORAGE", Offset(150f, 300f),
                mapOf("Integrity" to "100%", "Health" to "Stable"),
                listOf("Indices verified", "VACUUM optimized"),
            ),
            MaintenanceNodeState(
                "BRAIN", "Context Brain", "LOGIC", Offset(500f, 250f),
                mapOf("Aware" to "True", "Active" to "Yes"),
                listOf("Context synced: SETTINGS"),
            ),
            MaintenanceNodeState(
                "NLU", "Intent Engine", "INTENT", Offset(850f, 350f),
                mapOf("Conf" to "98%", "Lat" to "11ms"),
                listOf("Dialect Map: DEODHARI"),
            ),
            MaintenanceNodeState(
                "LLAMA", "Local LLM", "AGENTIC", Offset(850f, 150f),
                mapOf("Engine" to "llama.cpp", "RAM" to "2.4GB"),
                listOf("Ready for reasoning"),
            ),
            MaintenanceNodeState(
                "TESTS", "JUnit Suite", "QUALITY", Offset(500f, 500f),
                mapOf("Passed" to "57/57", "Rate" to "100%"),
                listOf("Regression: PASS"),
            ),
            MaintenanceNodeState(
                "UI", "Medical Hub", "PHYSICAL", Offset(1200f, 300f),
                mapOf("FPS" to "120", "Thread" to "Main"),
                listOf("Layer cache: HIT"),
            ),
        )
    }

    val selectedNode = nodes.find { it.id == selectedNodeId }

    val infiniteTransition = rememberInfiniteTransition(label = "flow_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Space Black
            .drawBehind {
                val gridStep = 50.dp.toPx()
                for (x in 0..size.width.toInt() step gridStep.toInt()) {
                    drawLine(
                        Color.White.copy(alpha = 0.05f),
                        Offset(x.toFloat(), 0f),
                        Offset(x.toFloat(), size.height),
                        strokeWidth = 1f,
                    )
                }
                for (y in 0..size.height.toInt() step gridStep.toInt()) {
                    drawLine(
                        Color.White.copy(alpha = 0.05f),
                        Offset(0f, y.toFloat()),
                        Offset(size.width, y.toFloat()),
                        strokeWidth = 1f,
                    )
                }

                // Draw Data Flow Connectors
                nodes.forEach { node ->
                    nodes.forEach { /** other */ other ->
                        if (node != other && isConnected(node.id, other.id)) {
                            drawLine(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2DD4BF), Color(0xFF38BDF8)),
                                ),
                                node.offset,
                                other.offset,
                                strokeWidth = 2f,
                                alpha = pulseAlpha,
                            )
                        }
                    }
                }
            },
    ) {
        // The Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        ) {
            nodes.forEach { node ->
                DraggableMaintenanceNode(
                    state = node,
                    isSelected = selectedNodeId == node.id,
                    onSelect = { selectedNodeId = node.id },
                    onDrag = { delta ->
                        node.offset += delta
                        // Force recompose for drawBehind
                        nodes[nodes.indexOf(node)] = node.copy()
                    },
                )
            }
        }

        // Overlay: Console Panel
        Box(modifier = Modifier.fillMaxSize()) {
            MaintenanceConsolePanel(
                selectedNode = selectedNode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            )

            HubControls(
                isSyncing = isSyncing,
                onBack = onNavigateBack,
                onDiagnosticRun = {
                    isSyncing = true
                    // Simulate system sweep
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
            )
        }
    }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            delay(2000.milliseconds)
            isSyncing = false
        }
    }
}

private fun isConnected(/** id1 */ id1: String, /** id2 */ id2: String): Boolean {
    val connections = setOf(
        "DB" to "BRAIN", "BRAIN" to "NLU", "NLU" to "LLAMA",
        "BRAIN" to "UI", "TESTS" to "BRAIN", "LLAMA" to "BRAIN",
    )
    return (id1 to id2) in connections || (id2 to id1) in connections
}

/**
 * Interactive node component that can be repositioned.
 */
@Composable
fun DraggableMaintenanceNode(
    /** component state */
    state: MaintenanceNodeState,
    /** highlighted flag */
    isSelected: Boolean,
    /** select handler */
    onSelect: () -> Unit,
    /** drag handler */
    onDrag: (Offset) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(state.offset.x.roundToInt(), state.offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .size(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer Glow
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawCircle(
                            Color(0xFF2DD4BF),
                            radius = size.width / 2,
                            style = Stroke(width = 4f),
                        )
                    },
            )
        }

        Surface(
            onClick = onSelect,
            shape = CircleShape,
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(
                2.dp, 
                if (isSelected) Color(0xFF2DD4BF) else Color(0xFF334155),
            ),
            modifier = Modifier.size(64.dp),
            tonalElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = state.id, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White, 
                    fontSize = 14.sp,
                )
            }
        }

        Text(
            text = state.name,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 24.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * UI controls for diagnostic hub.
 */
@Composable
fun HubControls(
    /** status */
    isSyncing: Boolean,
    /** exit */
    onBack: () -> Unit,
    /** runner */
    onDiagnosticRun: () -> Unit,
    /** UI customizer */
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack, 
                contentDescription = stringResource(R.string.cancel), 
                tint = Color.White,
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.height(40.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), 
                        strokeWidth = 2.dp, 
                        color = Color(0xFF2DD4BF),
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = if (isSyncing) "SUITE_EXECUTING..." else "MAINTENANCE_MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        IconButton(
            onClick = onDiagnosticRun,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF2DD4BF)),
        ) {
            Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black)
        }
    }
}

/**
 * Terminal-style output panel for system logs.
 */
@Composable
fun MaintenanceConsolePanel(
    /** focus */
    selectedNode: MaintenanceNodeState?,
    /** UI customizer */
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = selectedNode?.name ?: "System Master", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                    )
                    Text(
                        text = "Architectural Diagnostics & Functional Hub", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color(0xFF94A3B8),
                    )
                }
                if (selectedNode != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2DD4BF).copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = selectedNode.type,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2DD4BF),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (selectedNode == null) {
                    item { LogLine("> BOOT_SEQUENCE: CLINIC_LEDGER_OS_V3.0") }
                    item { LogLine("> PERF_BENCHMARK: 120FPS_VERIFIED") }
                    item { LogLine("> INTEGRITY_GUARDIAN: ACTIVE") }
                    item { 
                        LogLine(
                            "> STANDBY: SELECT NODE FOR COMPONENT TRACE", 
                            color = Color(0xFF94A3B8),
                        ) 
                    }
                } else {
                    items(selectedNode.logs) { log ->
                        LogLine("> TRACE: $log")
                    }
                    item {
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            selectedNode.metrics.forEach { (k, v) ->
                                Text(
                                    text = "$k: $v | ",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLine(/** msg */ text: String, /** tint */ color: Color = Color(0xFF22C55E)) {
    Text(
        text = text, 
        fontFamily = FontFamily.Monospace, 
        color = color, 
        fontSize = 11.sp,
    )
}
