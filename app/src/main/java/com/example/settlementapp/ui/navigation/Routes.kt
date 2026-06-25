package com.example.settlementapp.ui.navigation

object Routes {
    const val HOME = "home"
    const val MEETING_FORM = "meetingForm"        // ?meetingId=-1
    const val PICK_MEETING = "pickMeeting"        // /{purpose}
    const val PARTICIPANTS = "participants"       // /{meetingId}
    const val SETTLEMENT = "settlement"           // /{meetingId}
    const val MONTHLY = "monthly"

    const val ARG_MEETING_ID = "meetingId"
    const val ARG_PURPOSE = "purpose"

    const val PURPOSE_PARTICIPANT = "participant"
    const val PURPOSE_SETTLEMENT = "settlement"

    fun meetingForm(meetingId: Long = -1L) = "$MEETING_FORM?$ARG_MEETING_ID=$meetingId"
    fun pickMeeting(purpose: String) = "$PICK_MEETING/$purpose"
    fun participants(meetingId: Long) = "$PARTICIPANTS/$meetingId"
    fun settlement(meetingId: Long) = "$SETTLEMENT/$meetingId"
}
