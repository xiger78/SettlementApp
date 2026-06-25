package com.example.settlementapp.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.settlementapp.data.Gender
import com.example.settlementapp.data.PaymentType

enum class AppLanguage(val code: String, val nativeName: String) {
    KOREAN("ko", "한국어"),
    JAPANESE("ja", "日本語");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: KOREAN
    }
}

/**
 * 앱 전역 문자열. 언어별 인스턴스(KoStrings / JaStrings)를 CompositionLocal 로 제공한다.
 */
data class AppStrings(
    val lang: AppLanguage,

    // 공통
    val appName: String,
    val menu: String,
    val back: String,
    val save: String,
    val add: String,
    val edit: String,
    val delete: String,
    val open: String,
    val currency: String,
    val storeUnset: String,

    // 홈
    val menuMeetingTitle: String,
    val menuMeetingSubtitle: String,
    val menuParticipantTitle: String,
    val menuParticipantSubtitle: String,
    val menuSettlementTitle: String,
    val menuSettlementSubtitle: String,
    val menuMonthlyTitle: String,
    val menuMonthlySubtitle: String,
    val menuSettingsTitle: String,
    val menuSettingsSubtitle: String,
    val recentMeetings: String,
    val thisMonthTotal: String,
    val meetingCount: (Int) -> String,
    val homeEmpty: String,
    val participantsBadge: (Int) -> String,
    val settled: String,
    val unsettled: String,

    // 모임정보등록
    val meetingFormTitleNew: String,
    val meetingFormTitleEdit: String,
    val basicInfo: String,
    val meetingDate: String,
    val pickDate: String,
    val storeName: String,
    val storePhone: String,
    val headcount: String,
    val totalPeople: String,
    val male: String,
    val female: String,
    val headcountAutoNote: String,
    val otherContent: String,
    val memo: String,
    val saveAndAddParticipants: String,

    // 모임 선택
    val pickForSettlement: String,
    val pickForParticipant: String,
    val newMeeting: String,
    val pickerEmpty: String,
    val maleFemaleBadge: (Int, Int) -> String,

    // 참가자등록
    val participantsTitle: String,
    val meetingInfo: String,
    val registeredCount: String,
    val countWithGenders: (Int, Int, Int) -> String,
    val addParticipant: String,
    val name: String,
    val gender: String,
    val paymentType: String,
    val genderMale: String,
    val genderFemale: String,
    val cash: String,
    val paypay: String,
    val maxParticipantsNote: (Int) -> String,
    val participantList: String,
    val noParticipants: String,
    val goSettlement: String,

    // 정산
    val settlementTitle: String,
    val editParticipants: String,
    val participantCount: String,
    val amountCalc: String,
    val settlementAmountLabel: String,
    val femalePerPersonLabel: String,
    val femaleSumNote: (Int, String) -> String,
    val balanceLabel: String,
    val malePerPersonLabel: String,
    val enterFemaleFirst: String,
    val maleFormulaNote: String,
    val receipt: String,
    val capture: String,
    val recapture: String,
    val receiptHint: String,
    val participantSettlement: String,
    val noParticipantsSettlement: String,
    val settledProgress: (Int, Int) -> String,
    val completeSettlement: String,
    val savedHint: String,

    // 월별정산일람
    val monthlyTitle: String,
    val monthlyEmpty: String,
    val grandTotal: String,
    val monthFormat: (String, Int) -> String,

    // 설정
    val settingsTitle: String,
    val languageSetting: String,
    val language: String,
    val languageChangeNote: String
) {
    fun money(value: Long): String = "${groupNumber(value)}$currency"
    fun genderLabel(g: Gender): String = if (g == Gender.MALE) genderMale else genderFemale
    fun paymentLabel(p: PaymentType): String = if (p == PaymentType.CASH) cash else paypay
}

private val grouping = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
fun groupNumber(value: Long): String = grouping.format(value)

val KoStrings = AppStrings(
    lang = AppLanguage.KOREAN,
    appName = "정산앱",
    menu = "메뉴",
    back = "뒤로",
    save = "저장",
    add = "추가",
    edit = "수정",
    delete = "삭제",
    open = "열기",
    currency = "원",
    storeUnset = "가게 미입력",

    menuMeetingTitle = "모임정보등록",
    menuMeetingSubtitle = "모임날짜 · 가게 · 인원 등록",
    menuParticipantTitle = "참가자등록",
    menuParticipantSubtitle = "모임별 참가자 이름·성별·정산형태",
    menuSettlementTitle = "정산",
    menuSettlementSubtitle = "금액 계산 · 영수증 촬영 · 정산완료",
    menuMonthlyTitle = "월별정산일람",
    menuMonthlySubtitle = "월별 정산 합계 보기",
    menuSettingsTitle = "설정",
    menuSettingsSubtitle = "언어 설정",
    recentMeetings = "최근 모임",
    thisMonthTotal = "이번 달 정산 합계",
    meetingCount = { n -> "모임 ${n}건" },
    homeEmpty = "등록된 모임이 없습니다.\n‘모임정보등록’으로 시작하세요.",
    participantsBadge = { n -> "참가 ${n}명" },
    settled = "정산완료",
    unsettled = "미정산",

    meetingFormTitleNew = "모임정보등록",
    meetingFormTitleEdit = "모임정보 수정",
    basicInfo = "기본 정보",
    meetingDate = "모임날짜",
    pickDate = "날짜선택",
    storeName = "가게이름",
    storePhone = "가게전화번호",
    headcount = "참가인원",
    totalPeople = "총인원",
    male = "남자",
    female = "여자",
    headcountAutoNote = "※ 참가자등록 화면에서 이름을 추가하면 인원수가 자동 갱신됩니다.",
    otherContent = "기타내용",
    memo = "메모",
    saveAndAddParticipants = "등록하고 참가자 추가",

    pickForSettlement = "정산할 모임 선택",
    pickForParticipant = "참가자 등록할 모임 선택",
    newMeeting = "새 모임",
    pickerEmpty = "등록된 모임이 없습니다.\n먼저 모임을 등록하세요.",
    maleFemaleBadge = { m, f -> "남 $m · 여 $f" },

    participantsTitle = "참가자등록",
    meetingInfo = "모임 정보",
    registeredCount = "등록 인원",
    countWithGenders = { t, m, f -> "${t}명 (남 $m · 여 $f)" },
    addParticipant = "참가자 추가",
    name = "이름",
    gender = "성별",
    paymentType = "정산형태",
    genderMale = "남자",
    genderFemale = "여자",
    cash = "현금",
    paypay = "페이페이",
    maxParticipantsNote = { n -> "최대 ${n}명까지 등록할 수 있습니다." },
    participantList = "참가자 명단",
    noParticipants = "아직 등록된 참가자가 없습니다.",
    goSettlement = "정산하러 가기",

    settlementTitle = "정산",
    editParticipants = "참가자수정",
    participantCount = "참가인원",
    amountCalc = "금액 계산",
    settlementAmountLabel = "정산금액 (영수증 총액)",
    femalePerPersonLabel = "여자 1인 금액",
    femaleSumNote = { n, sum -> "여자 ${n}명 합계 $sum" },
    balanceLabel = "잔금액 (남자 부담 총액)",
    malePerPersonLabel = "남자 1인 금액 (자동계산)",
    enterFemaleFirst = "여자 금액 입력 후 계산",
    maleFormulaNote = "※ 남자 1인 금액 = (정산금액 − 여자 합계) ÷ 남자 인원수",
    receipt = "영수증",
    capture = "촬영",
    recapture = "다시 촬영",
    receiptHint = "카메라로 영수증을 촬영하세요",
    participantSettlement = "참가자 정산",
    noParticipantsSettlement = "참가자가 없습니다. 먼저 참가자를 등록하세요.",
    settledProgress = { done, total -> "정산 완료: $done / ${total}명" },
    completeSettlement = "정산완료",
    savedHint = "정산 내용이 저장되었습니다.",

    monthlyTitle = "월별정산일람",
    monthlyEmpty = "정산 내역이 없습니다.",
    grandTotal = "전체 정산 합계",
    monthFormat = { y, m -> "${y}년 ${m}월" },

    settingsTitle = "설정",
    languageSetting = "언어 설정",
    language = "언어",
    languageChangeNote = "선택한 언어로 메뉴와 화면이 즉시 변경됩니다."
)

val JaStrings = AppStrings(
    lang = AppLanguage.JAPANESE,
    appName = "精算アプリ",
    menu = "メニュー",
    back = "戻る",
    save = "保存",
    add = "追加",
    edit = "編集",
    delete = "削除",
    open = "開く",
    currency = "円",
    storeUnset = "店舗未入力",

    menuMeetingTitle = "集まり情報登録",
    menuMeetingSubtitle = "日付 · 店舗 · 人数を登録",
    menuParticipantTitle = "参加者登録",
    menuParticipantSubtitle = "参加者の名前・性別・精算方法",
    menuSettlementTitle = "精算",
    menuSettlementSubtitle = "金額計算 · レシート撮影 · 精算完了",
    menuMonthlyTitle = "月別精算一覧",
    menuMonthlySubtitle = "月別の精算合計を表示",
    menuSettingsTitle = "設定",
    menuSettingsSubtitle = "言語設定",
    recentMeetings = "最近の集まり",
    thisMonthTotal = "今月の精算合計",
    meetingCount = { n -> "集まり ${n}件" },
    homeEmpty = "登録された集まりがありません。\n「集まり情報登録」から始めましょう。",
    participantsBadge = { n -> "参加 ${n}名" },
    settled = "精算完了",
    unsettled = "未精算",

    meetingFormTitleNew = "集まり情報登録",
    meetingFormTitleEdit = "集まり情報の編集",
    basicInfo = "基本情報",
    meetingDate = "開催日",
    pickDate = "日付選択",
    storeName = "店舗名",
    storePhone = "店舗電話番号",
    headcount = "参加人数",
    totalPeople = "合計",
    male = "男性",
    female = "女性",
    headcountAutoNote = "※ 参加者登録画面で名前を追加すると人数が自動更新されます。",
    otherContent = "その他",
    memo = "メモ",
    saveAndAddParticipants = "登録して参加者を追加",

    pickForSettlement = "精算する集まりを選択",
    pickForParticipant = "参加者を登録する集まりを選択",
    newMeeting = "新規",
    pickerEmpty = "登録された集まりがありません。\nまず集まりを登録してください。",
    maleFemaleBadge = { m, f -> "男 $m · 女 $f" },

    participantsTitle = "参加者登録",
    meetingInfo = "集まり情報",
    registeredCount = "登録人数",
    countWithGenders = { t, m, f -> "${t}名 (男 $m · 女 $f)" },
    addParticipant = "参加者を追加",
    name = "名前",
    gender = "性別",
    paymentType = "精算方法",
    genderMale = "男性",
    genderFemale = "女性",
    cash = "現金",
    paypay = "PayPay",
    maxParticipantsNote = { n -> "最大 ${n}名まで登録できます。" },
    participantList = "参加者リスト",
    noParticipants = "まだ参加者が登録されていません。",
    goSettlement = "精算へ進む",

    settlementTitle = "精算",
    editParticipants = "参加者編集",
    participantCount = "参加人数",
    amountCalc = "金額計算",
    settlementAmountLabel = "精算金額 (レシート合計)",
    femalePerPersonLabel = "女性 1人あたり金額",
    femaleSumNote = { n, sum -> "女性 ${n}名 合計 $sum" },
    balanceLabel = "残額 (男性負担の合計)",
    malePerPersonLabel = "男性 1人あたり金額 (自動計算)",
    enterFemaleFirst = "女性金額の入力後に計算",
    maleFormulaNote = "※ 男性1人あたり = (精算金額 − 女性合計) ÷ 男性人数",
    receipt = "レシート",
    capture = "撮影",
    recapture = "再撮影",
    receiptHint = "カメラでレシートを撮影してください",
    participantSettlement = "参加者の精算",
    noParticipantsSettlement = "参加者がいません。先に参加者を登録してください。",
    settledProgress = { done, total -> "精算完了: $done / ${total}名" },
    completeSettlement = "精算完了",
    savedHint = "精算内容を保存しました。",

    monthlyTitle = "月別精算一覧",
    monthlyEmpty = "精算履歴がありません。",
    grandTotal = "全体の精算合計",
    monthFormat = { y, m -> "${y}年${m}月" },

    settingsTitle = "設定",
    languageSetting = "言語設定",
    language = "言語",
    languageChangeNote = "選択した言語にメニューと画面が即時変更されます。"
)

fun stringsFor(lang: AppLanguage): AppStrings = when (lang) {
    AppLanguage.KOREAN -> KoStrings
    AppLanguage.JAPANESE -> JaStrings
}

val LocalStrings = staticCompositionLocalOf { KoStrings }
