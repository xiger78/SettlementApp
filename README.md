# 정산앱 (SettlementApp)

모임 회비/정산을 관리하는 안드로이드 앱입니다. 회계 앱 느낌의 차분한 UI(딥 틸 그린 + 골드 포인트)와
Jetpack Compose · Material 3 로 제작되었습니다.

## 주요 기능 (메뉴)

| 메뉴 | 설명 |
| --- | --- |
| **모임정보등록** | 모임날짜 · 가게이름 · 가게전화번호 · 참가인원(남/여) · 기타내용 입력 |
| **참가자등록** | 모임을 선택하면 모임정보가 표시되고, 이름 · 성별 · 정산형태(현금/페이페이)로 참가자를 추가 (최대 30명, + 버튼) |
| **정산** | 모임을 선택하면 모임날짜·가게이름·참가인원수가 표시되고, 정산금액(영수증 총액)과 여자 1인 금액을 입력하면 잔금액·남자 1인 금액이 자동 계산됩니다. 참가자별로 금액·정산형태·정산완료를 체크 후 **정산완료** 저장. 카메라로 영수증 촬영/저장 가능 |
| **월별정산일람** | 월별 정산 합계(모임 수, 참가 인원, 정산금액)와 월별 모임 목록 |
| **설정** | 언어 설정 — **한국어 / 日本語** 선택 시 메뉴와 모든 화면이 즉시 전환 (선택값은 저장됨) |

## 다국어 지원 (한국어 / 日本語)
설정 화면에서 언어를 선택하면 앱 전체 UI가 즉시 해당 언어로 바뀝니다. 통화 단위(원/円)와 날짜 표기(2026년 6월 / 2026年6月)도 함께 현지화됩니다.

## 사용 설명서 (매뉴얼)
- 🇰🇷 [한국어 매뉴얼](docs/MANUAL_ko.md) — 기능별 설명 + 화면 캡처
- 🇯🇵 [日本語マニュアル](docs/MANUAL_ja.md) — 機能別の説明 + スクリーンショット

### 정산 금액 계산 규칙
- `정산금액` = 영수증 총액 (직접 입력, 기본 0원)
- `여자 합계` = `여자 1인 금액 × 여자 인원수`
- `잔금액` = `정산금액 − 여자 합계` (남자가 부담하는 총액)
- `남자 1인 금액` = `잔금액 ÷ 남자 인원수` (여자 금액 입력 후 자동 계산)

## 기술 스택
- Kotlin 2.0 / Jetpack Compose / Material 3
- Navigation Compose
- Room (로컬 DB) + KSP
- Coil (영수증 이미지 표시)
- 카메라 촬영: `ActivityResultContracts.TakePicture` + `FileProvider`

## 데이터 구조
- **Meeting (모임)**: 모임날짜, 가게이름, 가게전화번호, 총인원/남/여, 정산금액, 여자금액, 남자금액, 기타내용, 영수증 사진 URI
- **Participant (참가자)**: 모임 연결 ID, 이름, 성별, 정산형태(현금/페이페이), 금액, 정산완료 여부, 기타내용

## 빌드 / 실행 방법

### Android Studio (권장)
1. Android Studio에서 이 폴더(`SettlementApp`)를 엽니다.
2. SDK가 자동 인식되지 않으면 `local.properties`의 `sdk.dir` 경로를 본인 환경에 맞게 수정합니다.
3. 상단의 ▶(Run) 버튼으로 에뮬레이터 또는 실제 기기에 실행합니다.

### 커맨드라인
```bash
# JDK 17 필요
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=$HOME/Library/Android/sdk

# 디버그 APK 빌드
./gradlew :app:assembleDebug

# 결과물: app/build/outputs/apk/debug/app-debug.apk
```

연결된 기기에 바로 설치하려면:
```bash
./gradlew :app:installDebug
```

## 요구 사항
- Android Studio (Giraffe 이상 권장) / JDK 17
- Android SDK Platform 35 (compileSdk = 35), 최소 지원: Android 7.0 (minSdk = 24)

## 참고
- 영수증 사진은 앱 전용 외부 저장소(`Android/data/<package>/files/receipts`)에 저장됩니다.
- 참가자를 추가/삭제하면 모임의 인원수(총/남/여)가 자동으로 갱신됩니다.
