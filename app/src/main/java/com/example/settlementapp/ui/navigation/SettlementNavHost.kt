package com.example.settlementapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.settlementapp.ui.SettlementViewModel
import com.example.settlementapp.ui.screens.HomeScreen
import com.example.settlementapp.ui.screens.MeetingFormScreen
import com.example.settlementapp.ui.screens.MeetingPickerScreen
import com.example.settlementapp.ui.screens.MonthlyScreen
import com.example.settlementapp.ui.screens.ParticipantsScreen
import com.example.settlementapp.ui.screens.SettingsScreen
import com.example.settlementapp.ui.screens.SettlementScreen

@Composable
fun SettlementNavHost(viewModel: SettlementViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onRegisterMeeting = { navController.navigate(Routes.meetingForm()) },
                onRegisterParticipant = {
                    navController.navigate(Routes.pickMeeting(Routes.PURPOSE_PARTICIPANT))
                },
                onSettlement = {
                    navController.navigate(Routes.pickMeeting(Routes.PURPOSE_SETTLEMENT))
                },
                onMonthly = { navController.navigate(Routes.MONTHLY) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenMeeting = { id -> navController.navigate(Routes.settlement(id)) },
                onEditMeeting = { id -> navController.navigate(Routes.meetingForm(id)) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
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
                    // 새 모임 저장 후 참가자 등록 화면으로 이동
                    navController.popBackStack()
                    navController.navigate(Routes.participants(newId))
                }
            )
        }

        composable(
            route = "${Routes.PICK_MEETING}/{${Routes.ARG_PURPOSE}}",
            arguments = listOf(navArgument(Routes.ARG_PURPOSE) { type = NavType.StringType })
        ) { backStackEntry ->
            val purpose = backStackEntry.arguments?.getString(Routes.ARG_PURPOSE)
                ?: Routes.PURPOSE_PARTICIPANT
            MeetingPickerScreen(
                viewModel = viewModel,
                purpose = purpose,
                onBack = { navController.popBackStack() },
                onPicked = { id ->
                    if (purpose == Routes.PURPOSE_SETTLEMENT) {
                        navController.navigate(Routes.settlement(id))
                    } else {
                        navController.navigate(Routes.participants(id))
                    }
                },
                onCreateNew = { navController.navigate(Routes.meetingForm()) }
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

        composable(Routes.MONTHLY) {
            MonthlyScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenMeeting = { id -> navController.navigate(Routes.settlement(id)) }
            )
        }
    }
}
