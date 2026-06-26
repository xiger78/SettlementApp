#!/usr/bin/env python3
"""Capture Chinese settlement screenshots with same amounts as Korean manual."""
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KO_SCRIPT = os.path.join(ROOT, "scripts", "capture_manual_ko.py")

spec = importlib.util.spec_from_file_location("cap_ko", KO_SCRIPT)
cap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cap)

ZH = {
    "home_marker": "本月结算合计",
    "menu_meeting": "聚会信息登记",
    "menu_monthly": "月度结算一览",
    "menu_settings": "设置",
    "store_name": "店铺名称",
    "save_and_add": "登记并添加参与者",
    "add": "添加",
    "name": "姓名",
    "gender_female": "女性",
    "gender_male": "男性",
    "settlement_title": "结算",
    "settlement_amount": "结算金额 (收据合计)",
    "participant_settlement": "参与者结算",
}

cap.KO = ZH


def is_home_zh():
    t = cap.all_text()
    if "编辑聚会信息" in t or "基本信息" in t:
        return False
    if ZH["settlement_amount"] in t and "金额计算" in t:
        return False
    if "添加参与者" in t and ZH["participant_settlement"] not in t:
        if ZH["name"] in t and "性别" in t:
            return False
    if ZH["home_marker"] in t and "菜单" in t:
        return True
    if "最近聚会" in t and ZH["menu_settings"] in t:
        return True
    return False


cap.is_home = is_home_zh


def select_gender(male=True):
    label = ZH["gender_male"] if male else ZH["gender_female"]
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


def set_chinese():
    cap.clear_app()
    cap.launch(force_stop=True, cold=True)
    open_settings()
    cap.tap_row("中文", exact=True)
    cap.sleep(1.5)
    cap.sh("shell", "input", "keyevent", "4")
    cap.sleep(1.2)
    if not cap.wait_text(ZH["home_marker"]):
        cap.launch(force_stop=True, cold=False)
    if not cap.wait_text(ZH["home_marker"]):
        raise RuntimeError("Chinese language not applied")


def setup_data():
    cap.tap_menu(ZH["menu_meeting"])
    cap.sleep(3)
    cap.wait_text(ZH["store_name"])
    cap.tap_field(ZH["store_name"])
    cap.text("Sakura")
    cap.hide_keyboard()
    cap.fling_down()
    cap.fling_down()
    cap.tap_row(ZH["save_and_add"])
    cap.sleep(3)
    cap.wait_text(ZH["add"])
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
    cap.shot("04_settlement_calc_zh")

    cap.enter_settlement_amount(1, 5000)
    cap.wait_text("7,500", timeout=5)
    cap.shot("04_settlement_genderdiff_zh")

    cap.scroll_to_top()
    cap.enter_settlement_amount(2, 5000)
    cap.wait_text("15,000", timeout=5)
    cap.shot("04_settlement_split_zh")

    cap.hide_keyboard()
    for _ in range(10):
        if "Kim" in cap.all_text() and ZH["participant_settlement"] in cap.all_text():
            break
        cap.scroll_page()
    cap.shot("05_settlement_list_zh")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    print("=== Chinese settlement screenshots ===")
    set_chinese()
    setup_data()
    capture_settlement()
    print("=== Done ===")


if __name__ == "__main__":
    main()
