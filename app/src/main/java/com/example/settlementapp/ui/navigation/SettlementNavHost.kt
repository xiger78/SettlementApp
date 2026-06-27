package com.example.settlementapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.i18n.AppStrings
import com.example.settlementapp.ui.i18n.LocalStrings
import com.example.settlementapp.ui.screens.HomeScreen
import com.example.settlementapp.ui.screens.MeetingFormScreen
import com.example.settlementapp.ui.screens.MeetingPickerScreen
import com.example.settlementapp.ui.screens.MonthlyScreen
import com.example.settlementapp.ui.screens.ParticipantsScreen
import com.example.settlementapp.ui.screens.QuickParticipantsScreen
import com.example.settlementapp.ui.screens.SettingsScreen
import com.example.settlementapp.ui.screens.SettlementScreen

enum class BottomTab(
    val route: String,
    val icon: ImageVector,
    val title: (AppStrings) -> String
) {
    MEETING(
        route = Routes.TAB_MEETING,
        icon = Icons.AutoMirrored.Filled.EventNote,
        title = { it.menuMeetingTitle }
    ),
    PARTICIPANT(
        route = Routes.TAB_PARTICIPANT,
        icon = Icons.Filled.Groups,
        title = { it.menuParticipantTitle }
    ),
    SETTLEMENT(
        route = Routes.TAB_SETTLEMENT,
        icon = Icons.Filled.Receipt,
        title = { it.menuSettlementTitle }
    ),
    MONTHLY(
        route = Routes.TAB_MONTHLY,
        icon = Icons.Filled.CalendarMonth,
        title = { it.menuMonthlyTitle }
    );

    companion object {
        fun fromRoute(route: String?): BottomTab? =
            entries.firstOrNull { it.route == route }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementNavHost(viewModel: SettlementViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")
    val showShell = Routes.isBottomTabRoute(currentRoute)
    val currentTab = BottomTab.fromRoute(currentRoute) ?: BottomTab.MEETING
    val s = LocalStrings.current

    Scaffold(
        topBar = {
            if (showShell) {
                TopAppBar(
                    title = { Text(currentTab.title(s)) },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = s.menuSettingsTitle,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showShell) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == currentTab,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.TAB_MEETING) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title(s)) },
                            label = { Text(tab.title(s)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val contentPadding = if (showShell) innerPadding else PaddingValues()
        NavHost(
            navController = navController,
            startDestination = Routes.TAB_MEETING,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            composable(Routes.TAB_MEETING) {
                HomeScreen(
                    viewModel = viewModel,
                    onRegisterMeeting = { navController.navigate(Routes.meetingForm()) },
                    onOpenMeeting = { id -> navController.navigate(Routes.settlement(id)) },
                    onEditMeeting = { id -> navController.navigate(Routes.meetingForm(id)) }
                )
            }

            composable(Routes.TAB_PARTICIPANT) {
                MeetingPickerScreen(
                    viewModel = viewModel,
                    purpose = Routes.PURPOSE_PARTICIPANT,
                    isTabRoot = true,
                    onBack = { navController.navigate(Routes.TAB_MEETING) },
                    onPicked = { id -> navController.navigate(Routes.participants(id)) },
                    onCreateNew = { navController.navigate(Routes.meetingForm()) }
                )
            }

            composable(Routes.TAB_SETTLEMENT) {
                MeetingPickerScreen(
                    viewModel = viewModel,
                    purpose = Routes.PURPOSE_SETTLEMENT,
                    isTabRoot = true,
                    onBack = { navController.navigate(Routes.TAB_MEETING) },
                    onPicked = { id -> navController.navigate(Routes.settlement(id)) },
                    onCreateNew = { navController.navigate(Routes.meetingForm()) },
                    onQuickSettlement = {
                        viewModel.resetQuickSession()
                        navController.navigate(Routes.QUICK_PARTICIPANTS)
                    }
                )
            }

            composable(Routes.TAB_MONTHLY) {
                MonthlyScreen(
                    viewModel = viewModel,
                    isTabRoot = true,
                    onBack = { navController.navigate(Routes.TAB_MEETING) },
                    onOpenMeeting = { id -> navController.navigate(Routes.settlement(id)) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.QUICK_PARTICIPANTS) {
                QuickParticipantsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onGoSettlement = { navController.navigate(Routes.QUICK_SETTLEMENT) }
                )
            }

            composable(Routes.QUICK_SETTLEMENT) {
                SettlementScreen(
                    viewModel = viewModel,
                    meetingId = 0,
                    quickMode = true,
                    onBack = { navController.popBackStack() },
                    onEditParticipants = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Routes.MEETING_FORM}?${Routes.ARG_MEETING_ID}={${Routes.ARG_MEETING_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_MEETING_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val meetingId = backStackEntry.arguments?.getLong(Routes.ARG_MEETING_ID) ?: -1L
                MeetingFormScreen(
                    viewModel = viewModel,
                    meetingId = meetingId,
                    onBack = { navController.popBackStack() },
                    onSavedNew = { newId ->
                        navController.popBackStack()
                        navController.navigate(Routes.participants(newId))
                    }
                )
            }

            composable(
                route = "${Routes.PARTICIPANTS}/{${Routes.ARG_MEETING_ID}}",
                arguments = listOf(navArgument(Routes.ARG_MEETING_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val meetingId = backStackEntry.arguments?.getLong(Routes.ARG_MEETING_ID) ?: -1L
                ParticipantsScreen(
                    viewModel = viewModel,
                    meetingId = meetingId,
                    onBack = { navController.popBackStack() },
                    onGoSettlement = { navController.navigate(Routes.settlement(meetingId)) }
                )
            }

            composable(
                route = "${Routes.SETTLEMENT}/{${Routes.ARG_MEETING_ID}}",
                arguments = listOf(navArgument(Routes.ARG_MEETING_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val meetingId = backStackEntry.arguments?.getLong(Routes.ARG_MEETING_ID) ?: -1L
                SettlementScreen(
                    viewModel = viewModel,
                    meetingId = meetingId,
                    onBack = { navController.popBackStack() },
                    onEditParticipants = { navController.navigate(Routes.participants(meetingId)) }
                )
            }
        }
    }
}
