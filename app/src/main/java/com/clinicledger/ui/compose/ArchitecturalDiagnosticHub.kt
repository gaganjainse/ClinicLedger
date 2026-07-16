package com.clinicledger.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.ui.compose.components.ClinicScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class DiagnosticNode(
    val id: String,
    val name: String,
    val type: String,
    val x: Float,
    val y: Float,
    val metrics: Map<String, String>,
    val logs: List<String>,
)

/**
 * Native implementation of the Architectural Diagnostic Hub.
 * Replaces flow.jsx with high-fidelity Compose Canvas visuals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitecturalDiagnosticHub(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(value = false) }
    var selectedNodeId by remember { mutableStateOf("DB") }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val nodes = listOf(
        DiagnosticNode("DB", "SQLite Engine", "STORAGE", 100f, 200f, mapOf("Integrity" to "100%", "Version" to "5.0"), listOf("Index optimized", "VACUUM OK")),
        DiagnosticNode("BRAIN", "Context Brain", "LOGIC", 400f, 200f, mapOf("State" to "READY", "Aware" to "True"), listOf("Context: SETTINGS")),
        DiagnosticNode("NLU", "NLU Orchestrator", "INTENT", 700f, 200f, mapOf("Conf" to "96%", "Lat" to "14ms"), listOf("Resolved: SYS_DIAG")),
        DiagnosticNode("UI", "Medical UI", "PHYSICAL", 1000f, 200f, mapOf("FPS" to "120", "Theme" to "Med"), listOf("Layer sync OK"))
    )

    ClinicScaffold(
        title = "System Architecture",
        onBack = onNavigateBack,
        actions = {
            Button(
                onClick = { 
                    isSyncing = true
                    scope.launch {
                        delay(2000.milliseconds)
                        isSyncing = false
                    }
                },
                enabled = !isSyncing,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isSyncing) "DIAGNOSING..." else "RUN DIAGNOSTIC")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF020617)) // Dark background to match flow.jsx
        ) {
            // High-Fidelity Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(24.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    
                    // Draw connections
                    for (i in 0 until (nodes.size - 1)) {
                        val start = nodes[i]
                        val end = nodes[i+1]
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(start.x + 100, start.y + 50),
                            end = Offset(end.x, end.y + 50),
                            strokeWidth = 2f,
                            pathEffect = if (isSyncing) null else dashPathEffect
                        )
                        if (isSyncing) {
                            drawCircle(
                                color = Color(0xFF0D9488),
                                radius = 4f,
                                center = Offset(start.x + 100 + (end.x - (start.x + 100)) * pulseAlpha, start.y + 50)
                            )
                        }
                    }
                }

                // Interactive Nodes
                nodes.forEach { node ->
                    DiagnosticNodeView(
                        node = node,
                        isSelected = selectedNodeId == node.id,
                        onSelect = { selectedNodeId = node.id }
                    )
                }
            }

            // Details & Logs
            val selectedNode = nodes.find { it.id == selectedNodeId }
            selectedNode?.let { node ->
                NodeDetailSection(node)
            }
            
            Spacer(Modifier.weight(1f))
            
            // Footer Metrics
            SystemFooter()
        }
    }
}

@Composable
fun DiagnosticNodeView(node: DiagnosticNode, isSelected: Boolean, onSelect: () -> Unit) {
    val color = if (isSelected) Color(0xFF0D9488) else Color(0xFF1E293B)
    
    Surface(
        modifier = Modifier
            .offset(x = node.x.dp / 2, y = node.y.dp / 2) // Scaled for density
            .width(140.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(node.type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
            Text(node.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun NodeDetailSection(node: DiagnosticNode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("${node.name} Internals", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                node.metrics.forEach { (k, v) ->
                    Column {
                        Text(k.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(v, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Text("LIVE TRACE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
            node.logs.forEach { log ->
                Text("> $log", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF22C55E).copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun SystemFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A).copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("UPTIME: 142H 12M", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text("120FPS_STABLE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF22C55E))
        }
    }
}
