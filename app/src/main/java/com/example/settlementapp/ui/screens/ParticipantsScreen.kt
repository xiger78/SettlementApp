package com.example.settlementapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settlementapp.data.Gender
import com.example.settlementapp.data.Participant
import com.example.settlementapp.data.PaymentType
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.components.AppCard
import com.example.settlementapp.ui.components.InfoRow
import com.example.settlementapp.ui.components.SectionHeader
import com.example.settlementapp.ui.components.SegmentedChoice
import com.example.settlementapp.ui.i18n.AppStrings
import com.example.settlementapp.ui.i18n.LocalStrings
import com.example.settlementapp.ui.theme.PayPay
import com.example.settlementapp.ui.theme.Positive
import com.example.settlementapp.ui.theme.Teal500

private const val MAX_PARTICIPANTS = 30

private data class ParticipantDraft(
    val localId: Long,
    val name: String,
    val gender: Gender,
    val paymentType: PaymentType = PaymentType.CASH
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(
    viewModel: SettlementViewModel,
    meetingId: Long,
    onBack: () -> Unit,
    onGoSettlement: () -> Unit
) {
    val s = LocalStrings.current
    val meeting by viewModel.meetingFlow(meetingId).collectAsStateWithLifecycle(initialValue = null)
    val participants by viewModel.participantsFlow(meetingId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var maleCountInput by remember { mutableStateOf("") }
    var femaleCountInput by remember { mutableStateOf("") }
    val drafts = remember { mutableStateListOf<ParticipantDraft>() }
    val editedNames = remember { mutableStateMapOf<Long, String>() }

    val totalPlanned = participants.size + drafts.size
    val canAddMore = totalPlanned < MAX_PARTICIPANTS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.participantsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    IconButton(onClick = onGoSettlement) {
                        Icon(Icons.Filled.Receipt, contentDescription = s.settlementTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                AppCard {
                    SectionHeader(s.meetingInfo)
                    Spacer(Modifier.height(4.dp))
                    InfoRow(s.meetingDate, meeting?.meetingDate ?: "-")
                    InfoRow(s.storeName, meeting?.storeName?.ifBlank { "-" } ?: "-")
                    InfoRow(
                        s.registeredCount,
                        s.countWithGenders(
                            participants.size,
                            participants.count { it.gender == Gender.MALE },
                            participants.count { it.gender == Gender.FEMALE }
                        )
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(s.headcount)
                        Text(
                            "$totalPlanned / $MAX_PARTICIPANTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (canAddMore) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = maleCountInput,
                            onValueChange = { maleCountInput = it.filter { c -> c.isDigit() } },
                            label = { Text(s.male) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canAddMore,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = femaleCountInput,
                            onValueChange = { femaleCountInput = it.filter { c -> c.isDigit() } },
                            label = { Text(s.female) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canAddMore,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val maleN = maleCountInput.toIntOrNull() ?: 0
                            val femaleN = femaleCountInput.toIntOrNull() ?: 0
                            if (maleN + femaleN <= 0) return@Button
                            val slots = MAX_PARTICIPANTS - totalPlanned
                            var remaining = slots
                            (1..maleN).take(remaining).forEach { i ->
                                drafts.add(
                                    ParticipantDraft(
                                        localId = System.nanoTime() + i,
                                        name = s.defaultMaleName(i),
                                        gender = Gender.MALE
                                    )
                                )
                                remaining--
                            }
                            (1..femaleN).take(remaining).forEach { i ->
                                drafts.add(
                                    ParticipantDraft(
                                        localId = System.nanoTime() + i + 1000,
                                        name = s.defaultFemaleName(i),
                                        gender = Gender.FEMALE
                                    )
                                )
                            }
                            maleCountInput = ""
                            femaleCountInput = ""
                        },
                        enabled = canAddMore &&
                            ((maleCountInput.toIntOrNull() ?: 0) + (femaleCountInput.toIntOrNull() ?: 0) > 0),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(s.registerParticipants, style = MaterialTheme.typography.titleMedium)
                    }
                    if (!canAddMore) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            s.maxParticipantsNote(MAX_PARTICIPANTS),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                SectionHeader(s.participantList)
                Spacer(Modifier.height(4.dp))
            }

            if (drafts.isEmpty() && participants.isEmpty()) {
                item {
                    AppCard {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                s.noParticipants,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(drafts.size, key = { drafts[it].localId }) { index ->
                val draft = drafts[index]
                DraftParticipantRow(
                    strings = s,
                    draft = draft,
                    onNameChange = { drafts[index] = draft.copy(name = it) },
                    onPaymentChange = { drafts[index] = draft.copy(paymentType = it) },
                    onSave = {
                        if (draft.name.isNotBlank()) {
                            viewModel.addParticipant(
                                meetingId,
                                draft.name,
                                draft.gender,
                                draft.paymentType
                            )
                            drafts.removeAt(index)
                        }
                    },
                    onDelete = { drafts.removeAt(index) }
                )
                Spacer(Modifier.height(10.dp))
            }

            items(participants, key = { it.id }) { p ->
                val displayName = editedNames[p.id] ?: p.name
                ParticipantEditRow(
                    strings = s,
                    index = participants.indexOf(p) + 1,
                    name = displayName,
                    onNameChange = { editedNames[p.id] = it },
                    participant = p,
                    onGenderChange = { viewModel.updateParticipant(p.copy(gender = it)) },
                    onPaymentChange = { viewModel.updateParticipant(p.copy(paymentType = it)) },
                    onSave = {
                        val newName = editedNames[p.id]?.trim().orEmpty()
                        if (newName.isNotBlank() && newName != p.name) {
                            viewModel.updateParticipant(p.copy(name = newName))
                        }
                        editedNames.remove(p.id)
                    },
                    onDelete = {
                        editedNames.remove(p.id)
                        viewModel.deleteParticipant(p)
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                OutlinedButton(
                    onClick = onGoSettlement,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(s.goSettlement, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DraftParticipantRow(
    strings: AppStrings,
    draft: ParticipantDraft,
    onNameChange: (String) -> Unit,
    onPaymentChange: (PaymentType) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings.genderLabel(draft.gender),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedTextField(
                value = draft.name,
                onValueChange = onNameChange,
                label = { Text(strings.name) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SegmentedChoice(
                options = listOf(PaymentType.CASH to strings.cash, PaymentType.PAYPAY to strings.paypay),
                selected = draft.paymentType,
                onSelect = onPaymentChange,
                activeColor = if (draft.paymentType == PaymentType.CASH) Positive else PayPay,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSave,
                enabled = draft.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(strings.saveParticipant)
            }
        }
    }
}

@Composable
private fun ParticipantEditRow(
    strings: AppStrings,
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    participant: Participant,
    onGenderChange: (Gender) -> Unit,
    onPaymentChange: (PaymentType) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        Text("$index", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    strings.genderLabel(participant.gender),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(strings.name) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            SegmentedChoice(
                options = listOf(Gender.MALE to strings.genderMale, Gender.FEMALE to strings.genderFemale),
                selected = participant.gender,
                onSelect = onGenderChange,
                activeColor = Teal500,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SegmentedChoice(
                options = listOf(PaymentType.CASH to strings.cash, PaymentType.PAYPAY to strings.paypay),
                selected = participant.paymentType,
                onSelect = onPaymentChange,
                activeColor = if (participant.paymentType == PaymentType.CASH) Positive else PayPay,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSave,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(strings.saveParticipant)
            }
        }
    }
}
