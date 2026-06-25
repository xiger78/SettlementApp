package com.example.settlementapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.settlementapp.data.Meeting
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.components.AppCard
import com.example.settlementapp.ui.components.DatePickerModal
import com.example.settlementapp.ui.components.SectionHeader
import com.example.settlementapp.ui.i18n.LocalStrings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingFormScreen(
    viewModel: SettlementViewModel,
    meetingId: Long,
    onBack: () -> Unit,
    onSavedNew: (Long) -> Unit
) {
    val s = LocalStrings.current
    val isEdit = meetingId > 0
    val meetingFlow = remember(meetingId) {
        if (isEdit) viewModel.meetingFlow(meetingId) else flowOf(null)
    }
    val existing by meetingFlow.collectAsStateWithLifecycle(initialValue = null)

    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var storeName by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var male by remember { mutableStateOf("") }
    var female by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val e = existing
        if (isEdit && e != null && !loaded) {
            date = e.meetingDate
            storeName = e.storeName
            storePhone = e.storePhone
            total = if (e.totalCount > 0) e.totalCount.toString() else ""
            male = if (e.maleCount > 0) e.maleCount.toString() else ""
            female = if (e.femaleCount > 0) e.femaleCount.toString() else ""
            note = e.note
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) s.meetingFormTitleEdit else s.meetingFormTitleNew) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AppCard {
                SectionHeader(s.basicInfo)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s.meetingDate) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = s.pickDate)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(s.storeName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = storePhone,
                    onValueChange = { storePhone = it },
                    label = { Text(s.storePhone) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(14.dp))

            AppCard {
                SectionHeader(s.headcount)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = total,
                        onValueChange = { v -> total = v.filter { it.isDigit() } },
                        label = { Text(s.totalPeople) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = male,
                        onValueChange = { v -> male = v.filter { it.isDigit() } },
                        label = { Text(s.male) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = female,
                        onValueChange = { v -> female = v.filter { it.isDigit() } },
                        label = { Text(s.female) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    s.headcountAutoNote,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))

            AppCard {
                SectionHeader(s.otherContent)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(s.memo) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val meeting = Meeting(
                        id = if (isEdit) meetingId else 0,
                        meetingDate = date,
                        storeName = storeName.trim(),
                        storePhone = storePhone.trim(),
                        totalCount = total.toIntOrNull() ?: 0,
                        maleCount = male.toIntOrNull() ?: 0,
                        femaleCount = female.toIntOrNull() ?: 0,
                        note = note.trim(),
                        settlementAmount = existing?.settlementAmount ?: 0,
                        femaleAmount = existing?.femaleAmount ?: 0,
                        maleAmount = existing?.maleAmount ?: 0,
                        receiptPhotoUri = existing?.receiptPhotoUri
                    )
                    viewModel.saveMeeting(meeting) { newId ->
                        if (isEdit) onBack() else onSavedNew(newId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) s.save else s.saveAndAddParticipants, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date = it }
        )
    }
}
