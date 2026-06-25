package com.example.settlementapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.settlementapp.data.Gender
import com.example.settlementapp.data.Participant
import com.example.settlementapp.data.PaymentType
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.components.AppCard
import com.example.settlementapp.ui.components.InfoRow
import com.example.settlementapp.ui.components.Pill
import com.example.settlementapp.ui.components.SectionHeader
import com.example.settlementapp.ui.components.SegmentedChoice
import com.example.settlementapp.ui.theme.Gold
import com.example.settlementapp.ui.theme.PayPay
import com.example.settlementapp.ui.theme.Positive
import com.example.settlementapp.util.ReceiptFiles
import com.example.settlementapp.util.toAmountLong
import com.example.settlementapp.util.toWon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    viewModel: SettlementViewModel,
    meetingId: Long,
    onBack: () -> Unit,
    onEditParticipants: () -> Unit
) {
    val context = LocalContext.current
    val meeting by viewModel.meetingFlow(meetingId).collectAsStateWithLifecycle(initialValue = null)
    val dbParticipants by viewModel.participantsFlow(meetingId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // 입력 상태
    var settlementText by remember { mutableStateOf("") }   // 정산금액(영수증 총액)
    var femaleText by remember { mutableStateOf("") }       // 여자 1인 금액
    var seededAmount by remember { mutableStateOf(false) }

    // 참가자 로컬 상태 (정산형태/정산완료 체크)
    val localParticipants = remember { mutableStateListOf<Participant>() }
    var seededParticipants by remember { mutableStateOf(false) }

    var showSavedHint by remember { mutableStateOf(false) }

    LaunchedEffect(meeting) {
        val m = meeting
        if (m != null && !seededAmount) {
            settlementText = if (m.settlementAmount > 0) m.settlementAmount.toString() else ""
            femaleText = if (m.femaleAmount > 0) m.femaleAmount.toString() else ""
            seededAmount = true
        }
    }
    LaunchedEffect(dbParticipants) {
        if (!seededParticipants && dbParticipants.isNotEmpty()) {
            localParticipants.clear()
            localParticipants.addAll(dbParticipants)
            seededParticipants = true
        }
    }

    val settlementAmount = settlementText.toAmountLong()
    val femaleAmount = femaleText.toAmountLong()
    val femaleCount = localParticipants.count { it.gender == Gender.FEMALE }
    val maleCount = localParticipants.count { it.gender == Gender.MALE }
    val femaleTotal = femaleAmount * femaleCount
    val balance = (settlementAmount - femaleTotal).coerceAtLeast(0)   // 잔금액
    val maleAmount = SettlementViewModel.computeMaleAmount(
        settlementAmount, femaleAmount, femaleCount, maleCount
    )

    // 영수증 촬영
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingUri != null) {
            viewModel.setReceiptPhoto(meetingId, pendingUri.toString())
        }
        pendingUri = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("정산") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onEditParticipants) {
                        Icon(Icons.Filled.Edit, contentDescription = "참가자수정")
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
            // 1~3. 모임 정보
            item {
                AppCard {
                    SectionHeader("모임 정보")
                    Spacer(Modifier.height(4.dp))
                    InfoRow("모임날짜", meeting?.meetingDate ?: "-")
                    InfoRow("가게이름", meeting?.storeName?.ifBlank { "-" } ?: "-")
                    InfoRow(
                        "참가인원",
                        "${localParticipants.size}명 (남 $maleCount · 여 $femaleCount)"
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // 4~6. 금액 계산
            item {
                AppCard {
                    SectionHeader("금액 계산")
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = settlementText,
                        onValueChange = { v -> settlementText = v.filter { it.isDigit() } },
                        label = { Text("정산금액 (영수증 총액)") },
                        placeholder = { Text("0") },
                        suffix = { Text("원") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text(settlementAmount.toWon()) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = femaleText,
                        onValueChange = { v -> femaleText = v.filter { it.isDigit() } },
                        label = { Text("여자 1인 금액") },
                        placeholder = { Text("0") },
                        suffix = { Text("원") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text("여자 ${femaleCount}명 합계 ${femaleTotal.toWon()}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))
                    CalcResultRow("잔금액 (남자 부담 총액)", balance.toWon(), Gold)
                    Spacer(Modifier.height(8.dp))
                    CalcResultRow(
                        "남자 1인 금액 (자동계산)",
                        if (femaleText.isBlank()) "여자 금액 입력 후 계산" else maleAmount.toWon(),
                        Positive
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "※ 남자 1인 금액 = (정산금액 − 여자 합계) ÷ 남자 인원수",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // 7. 영수증 사진
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader("영수증")
                        OutlinedButton(onClick = {
                            val uri = ReceiptFiles.newReceiptUri(context)
                            pendingUri = uri
                            cameraLauncher.launch(uri)
                        }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (meeting?.receiptPhotoUri == null) "촬영" else "다시 촬영")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val photo = meeting?.receiptPhotoUri
                    if (photo != null) {
                        AsyncImage(
                            model = photo,
                            contentDescription = "영수증 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.PhotoCamera,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "카메라로 영수증을 촬영하세요",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                SectionHeader("참가자 정산")
                Spacer(Modifier.height(4.dp))
            }

            // 6(아래). 참가자 정산 목록
            if (localParticipants.isEmpty()) {
                item {
                    AppCard {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "참가자가 없습니다. 먼저 참가자를 등록하세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(localParticipants, key = { it.id }) { p ->
                    val amount = if (p.gender == Gender.FEMALE) femaleAmount else maleAmount
                    SettlementParticipantRow(
                        participant = p,
                        amount = amount,
                        onTogglePayment = {
                            val idx = localParticipants.indexOfFirst { it.id == p.id }
                            if (idx >= 0) {
                                val next = if (p.paymentType == PaymentType.CASH) PaymentType.PAYPAY else PaymentType.CASH
                                localParticipants[idx] = p.copy(paymentType = next)
                            }
                        },
                        onToggleSettled = {
                            val idx = localParticipants.indexOfFirst { it.id == p.id }
                            if (idx >= 0) localParticipants[idx] = p.copy(isSettled = !p.isSettled)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                val settledCount = localParticipants.count { it.isSettled }
                Text(
                    "정산 완료: $settledCount / ${localParticipants.size}명",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.completeSettlement(
                            meetingId = meetingId,
                            settlementAmount = settlementAmount,
                            femaleAmount = femaleAmount,
                            participantsUi = localParticipants.toList()
                        )
                        showSavedHint = true
                    },
                    enabled = settlementAmount > 0,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("정산완료", style = MaterialTheme.typography.titleMedium)
                }
                if (showSavedHint) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "정산 내용이 저장되었습니다.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Positive
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CalcResultRow(label: String, value: String, accent: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun SettlementParticipantRow(
    participant: Participant,
    amount: Long,
    onTogglePayment: () -> Unit,
    onToggleSettled: () -> Unit
) {
    val settledBg = if (participant.isSettled) Positive.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = settledBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSettled) {
                Icon(
                    if (participant.isSettled) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "정산여부",
                    tint = if (participant.isSettled) Positive else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        participant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Pill(
                        text = participant.gender.label,
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    amount.toWon(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            // 정산형태 토글
            val payColor = if (participant.paymentType == PaymentType.CASH) Positive else PayPay
            Pill(
                text = participant.paymentType.label,
                container = payColor.copy(alpha = 0.15f),
                contentColor = payColor,
                modifier = Modifier.clickable { onTogglePayment() }
            )
        }
    }
}
