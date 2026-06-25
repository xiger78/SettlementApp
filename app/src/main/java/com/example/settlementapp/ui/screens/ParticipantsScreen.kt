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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var payment by remember { mutableStateOf(PaymentType.CASH) }

    val canAdd = participants.size < MAX_PARTICIPANTS

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
            // 모임 정보
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

            // 참가자 추가
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(s.addParticipant)
                        Text(
                            "${participants.size} / $MAX_PARTICIPANTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (canAdd) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(s.name) },
                        singleLine = true,
                        enabled = canAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(s.gender, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    SegmentedChoice(
                        options = listOf(Gender.MALE to s.genderMale, Gender.FEMALE to s.genderFemale),
                        selected = gender,
                        onSelect = { gender = it },
                        activeColor = Teal500
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(s.paymentType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    SegmentedChoice(
                        options = listOf(PaymentType.CASH to s.cash, PaymentType.PAYPAY to s.paypay),
                        selected = payment,
                        onSelect = { payment = it },
                        activeColor = if (payment == PaymentType.CASH) Positive else PayPay
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && canAdd) {
                                viewModel.addParticipant(meetingId, name, gender, payment)
                                name = ""
                            }
                        },
                        enabled = canAdd && name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(s.add, style = MaterialTheme.typography.titleMedium)
                    }
                    if (!canAdd) {
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

            if (participants.isEmpty()) {
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
            } else {
                items(participants, key = { it.id }) { p ->
                    ParticipantEditRow(
                        strings = s,
                        index = participants.indexOf(p) + 1,
                        participant = p,
                        onGenderChange = { viewModel.updateParticipant(p.copy(gender = it)) },
                        onPaymentChange = { viewModel.updateParticipant(p.copy(paymentType = it)) },
                        onDelete = { viewModel.deleteParticipant(p) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
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
private fun ParticipantEditRow(
    strings: AppStrings,
    index: Int,
    participant: Participant,
    onGenderChange: (Gender) -> Unit,
    onPaymentChange: (PaymentType) -> Unit,
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
                    participant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SegmentedChoice(
                    options = listOf(Gender.MALE to strings.genderMale, Gender.FEMALE to strings.genderFemale),
                    selected = participant.gender,
                    onSelect = onGenderChange,
                    activeColor = Teal500,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            SegmentedChoice(
                options = listOf(PaymentType.CASH to strings.cash, PaymentType.PAYPAY to strings.paypay),
                selected = participant.paymentType,
                onSelect = onPaymentChange,
                activeColor = if (participant.paymentType == PaymentType.CASH) Positive else PayPay,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
