package com.clinicledger.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.voice.IntentType
import com.clinicledger.voice.ParsedVoiceIntent

/**
 * Visual summary of detected intent for user confirmation.
 */
@Composable
fun IntentConfirmation(
    intent: ParsedVoiceIntent,
    patients: List<Patient>,
    onConfirm: (Long?) -> Unit,
    onCancel: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.confirm_action_title), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val annotatedText = buildAnnotatedString {
            intent.medicineAmount?.let { 
                append(stringResource(R.string.medicine_added_label, LocaleManager.formatCurrency(it))) 
            }
            intent.paymentAmount?.let { 
                if (length > 0) append(" & ")
                append(stringResource(R.string.payment_added_label, LocaleManager.formatCurrency(it))) 
            }
            if (intent.intent == IntentType.SEARCH_BALANCE) {
                append(stringResource(R.string.check_balance_label))
            }
            if (intent.intent == IntentType.SUMMARY) {
                append(stringResource(R.string.today_activities_label))
            }
        }
        Text(annotatedText, style = MaterialTheme.typography.bodyLarge)
        
        if (patients.size > 1) {
            Spacer(Modifier.height(16.dp))
            patients.forEach { p ->
                OutlinedButton(
                    onClick = { onConfirm(p.id) }, 
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { 
                    Text("${p.name} (${p.village?.name ?: ""})") 
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), 
                modifier = Modifier.padding(top = 24.dp)
            ) {
                OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.no_text)) }
                Button(onClick = { onConfirm(null) }) { Text(stringResource(R.string.yes_text)) }
            }
        }
    }
}

/**
 * Overlay for mapping unknown commands to clinical actions.
 */
@Composable
fun TeachingOverlay(onSelectAction: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.teaching_prompt), 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        val options = listOf("MEDICINE", "PAYMENT", "BALANCE")
        options.forEach { id ->
            OutlinedButton(
                onClick = { onSelectAction(id) }, 
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) { 
                Text(id) 
            }
        }
    }
}
