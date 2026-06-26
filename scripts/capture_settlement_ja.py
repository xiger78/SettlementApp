#!/usr/bin/env python3
"""Capture Japanese settlement screenshots with same amounts as Korean manual."""
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KO_SCRIPT = os.path.join(ROOT, "scripts", "capture_manual_ko.py")

spec = importlib.util.spec_from_file_location("cap_ko", KO_SCRIPT)
cap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cap)

JA = {
    "home_marker": "今月の精算合計",
    "menu_meeting": "集まり情報登録",
    "menu_monthly": "月別精算一覧",
    "menu_settings": "設定",
    "store_name": "店舗名",
    "save_and_add": "登録して参加者を追加",
    "add": "追加",
    "name": "名前",
    "gender_female": "女性",
    "gender_male": "男性",
    "settlement_title": "精算",
    "settlement_amount": "精算金額 (レシート合計)",
    "participant_settlement": "参加者の精算",
}

cap.KO = JA


def is_home_ja():
    t = cap.all_text()
    if "集まり情報の編集" in t or "基本情報" in t:
        return False
    if JA["settlement_amount"] in t and "金額計算" in t:
        return False
    if "参加者を追加" in t and JA["participant_settlement"] not in t:
        if JA["name"] in t and "性別" in t:
            return False
    if JA["home_marker"] in t and "メニュー" in t:
        return True
    if "最近の集まり" in t and JA["menu_settings"] in t:
        return True
    return False


cap.is_home = is_home_ja


def select_gender(male=True):
    label = JA["gender_male"] if male else JA["gender_female"]
    for text, desc, cls, clickable, bounds, c in cap.visible_nodes():
        if text == label and clickable == "true":
            cap.tap_xy(*c)
            return
    cap.tap_row(label, exact=True)


cap.select_gender = select_gender


def open_settings():
    for label in ("Settings", "설정", "設定", "设置"):
        if label in cap.all_text():
            cap.tap_menu(label)
            cap.sleep(2)
            return
    raise RuntimeError("Settings menu not found")


def set_japanese():
    cap.clear_app()
    cap.launch(force_stop=True, cold=True)
    open_settings()
    cap.tap_row("日本語", exact=True)
    cap.sleep(1.5)
    cap.sh("shell", "input", "keyevent", "4")
    cap.sleep(1.2)
    if not cap.wait_text(JA["home_marker"]):
        cap.launch(force_stop=True, cold=False)
    if not cap.wait_text(JA["home_marker"]):
        raise RuntimeError("Japanese language not applied")


def setup_data():
    cap.tap_menu(JA["menu_meeting"])
    cap.sleep(3)
    cap.wait_text(JA["store_name"])
    cap.tap_field(JA["store_name"])
    cap.text("Sakura")
    cap.hide_keyboard()
    cap.fling_down()
    cap.fling_down()
    cap.tap_row(JA["save_and_add"])
    cap.sleep(3)
    cap.wait_text(JA["add"])
    cap.add_participant("Kim", female=False)
    cap.add_participant("Lee", female=False)
    cap.add_participant("Park", female=True)
    cap.add_participant("Choi", female=True)
    if "4 / 30" not in cap.all_text():
        raise RuntimeError(f"Failed to register participants: {cap.all_text()[:200]!r}")
    cap.sleep(2)
    cap.open_settlement()


def capture_settlement():
    cap.enter_settlement_amount(0, 40000)
    cap.wait_text("10,000", timeout=5)
    cap.shot("04_settlement_calc_ja")

    cap.enter_settlement_amount(1, 5000)
    cap.wait_text("7,500", timeout=5)
    cap.shot("04_settlement_genderdiff_ja")

    cap.scroll_to_top()
    cap.enter_settlement_amount(2, 5000)
    cap.wait_text("15,000", timeout=5)
    cap.shot("04_settlement_split_ja")

    cap.hide_keyboard()
    for _ in range(10):
        if "Kim" in cap.all_text() and JA["participant_settlement"] in cap.all_text():
            break
        cap.scroll_page()
    cap.shot("05_settlement_list_ja")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    print("=== Japanese settlement screenshots ===")
    set_japanese()
    setup_data()
    capture_settlement()
    print("=== Done ===")


if __name__ == "__main__":
    main()
