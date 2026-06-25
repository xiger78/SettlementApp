package com.example.settlementapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settlementapp.data.Meeting
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.components.AppCard
import com.example.settlementapp.ui.components.EmptyState
import com.example.settlementapp.ui.components.MenuCard
import com.example.settlementapp.ui.components.Pill
import com.example.settlementapp.ui.components.SectionHeader
import com.example.settlementapp.ui.theme.Gold
import com.example.settlementapp.ui.theme.PayPay
import com.example.settlementapp.ui.theme.Positive
import com.example.settlementapp.ui.theme.Teal500
import com.example.settlementapp.util.toWon
import java.time.YearMonth

@Composable
fun HomeScreen(
    viewModel: SettlementViewModel,
    onRegisterMeeting: () -> Unit,
    onRegisterParticipant: () -> Unit,
    onSettlement: () -> Unit,
    onMonthly: () -> Unit,
    onOpenMeeting: (Long) -> Unit,
    onEditMeeting: (Long) -> Unit
) {
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()

    val currentMonth = YearMonth.now().toString() // yyyy-MM
    val thisMonthMeetings = meetings.filter { it.meetingDate.startsWith(currentMonth) }
    val thisMonthTotal = thisMonthMeetings.sumOf { it.settlementAmount }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { HomeHeader(thisMonthTotal, thisMonthMeetings.size) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader("메뉴")
                    Spacer(Modifier.height(8.dp))
                    MenuCard(
                        title = "모임정보등록",
                        subtitle = "모임날짜 · 가게 · 인원 등록",
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        accent = Teal500,
                        onClick = onRegisterMeeting
                    )
                    Spacer(Modifier.height(10.dp))
                    MenuCard(
                        title = "참가자등록",
                        subtitle = "모임별 참가자 이름·성별·정산형태",
                        icon = Icons.Filled.Groups,
                        accent = Positive,
                        onClick = onRegisterParticipant
                    )
                    Spacer(Modifier.height(10.dp))
                    MenuCard(
                        title = "정산",
                        subtitle = "금액 계산 · 영수증 촬영 · 정산완료",
                        icon = Icons.Filled.Receipt,
                        accent = Gold,
                        onClick = onSettlement
                    )
                    Spacer(Modifier.height(10.dp))
                    MenuCard(
                        title = "월별정산일람",
                        subtitle = "월별 정산 합계 보기",
                        icon = Icons.Filled.CalendarMonth,
                        accent = PayPay,
                        onClick = onMonthly
                    )
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("최근 모임")
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (meetings.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        AppCard {
                            EmptyState(
                                text = "등록된 모임이 없습니다.\n‘모임정보등록’으로 시작하세요.",
                                icon = Icons.Filled.Inbox
                            )
                        }
                    }
                }
            } else {
                items(meetings, key = { it.id }) { meeting ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        MeetingRowCard(
                            meeting = meeting,
                            onClick = { onOpenMeeting(meeting.id) },
                            onEdit = { onEditMeeting(meeting.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(thisMonthTotal: Long, meetingCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "정산앱",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "이번 달 정산 합계",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                thisMonthTotal.toWon(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "모임 ${meetingCount}건",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun MeetingRowCard(
    meeting: Meeting,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(meeting.meetingDate, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    meeting.storeName.ifBlank { "가게 미입력" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(
                        text = "참가 ${meeting.totalCount}명",
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (meeting.settlementAmount > 0) {
                        Pill(
                            text = "정산완료",
                            container = Positive.copy(alpha = 0.15f),
                            contentColor = Positive
                        )
                    } else {
                        Pill(
                            text = "미정산",
                            container = Gold.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    meeting.settlementAmount.toWon(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ListAlt,
                        contentDescription = "수정",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("수정", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
