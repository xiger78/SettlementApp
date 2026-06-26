#!/usr/bin/env python3
"""Capture English settlement screenshots with same amounts as Korean manual."""
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KO_SCRIPT = os.path.join(ROOT, "scripts", "capture_manual_ko.py")

spec = importlib.util.spec_from_file_location("cap_ko", KO_SCRIPT)
cap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cap)

EN = {
    "home_marker": "This Month Total",
    "menu_meeting": "Register Event",
    "menu_monthly": "Monthly Summary",
    "menu_settings": "Settings",
    "store_name": "Venue",
    "save_and_add": "Save & Add Participants",
    "add": "Add",
    "name": "Name",
    "gender_female": "Female",
    "gender_male": "Male",
    "settlement_title": "Settlement",
    "settlement_amount": "Total (Receipt)",
    "participant_settlement": "Participant Settlement",
}

# Reuse helpers from Korean script with English labels
cap.KO = EN


def is_home_en():
    t = cap.all_text()
    if "Edit Event" in t or "Basic Info" in t:
        return False
    if "Total (Receipt)" in t and "Amount Calculation" in t:
        return False
    if "Add Participant" in t and "Participant Settlement" not in t:
        if "Name" in t and "Gender" in t:
            return False
    if EN["home_marker"] in t and "Menu" in t:
        return True
    if "Recent Events" in t and "Settings" in t:
        return True
    return False


cap.is_home = is_home_en


def select_gender(male=True):
    label = EN["gender_male"] if male else EN["gender_female"]
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


def set_english():
    cap.clear_app()
    cap.launch(force_stop=True, cold=True)
    open_settings()
    cap.tap_row("English", exact=True)
    cap.sleep(1.5)
    cap.sh("shell", "input", "keyevent", "4")
    cap.sleep(1.2)
    if not cap.wait_text(EN["home_marker"]):
        cap.launch(force_stop=True, cold=False)
    if not cap.wait_text(EN["home_marker"]):
        raise RuntimeError("English language not applied")


def setup_data():
    cap.tap_menu(EN["menu_meeting"])
    cap.sleep(3)
    cap.wait_text(EN["store_name"])
    cap.tap_field(EN["store_name"])
    cap.text("Sakura")
    cap.hide_keyboard()
    cap.fling_down()
    cap.fling_down()
    cap.tap_row(EN["save_and_add"])
    cap.sleep(3)
    cap.wait_text(EN["add"])
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
    cap.shot("04_settlement_calc_en")

    cap.enter_settlement_amount(1, 5000)
    cap.wait_text("7,500", timeout=5)
    cap.shot("04_settlement_genderdiff_en")

    cap.scroll_to_top()
    cap.enter_settlement_amount(2, 5000)
    cap.wait_text("15,000", timeout=5)
    cap.shot("04_settlement_split_en")

    cap.hide_keyboard()
    for _ in range(10):
        if "Kim" in cap.all_text() and EN["participant_settlement"] in cap.all_text():
            break
        cap.scroll_page()
    cap.shot("05_settlement_list_en")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    print("=== English settlement screenshots ===")
    set_english()
    setup_data()
    capture_settlement()
    print("=== Done ===")


if __name__ == "__main__":
    main()
