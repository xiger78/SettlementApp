#!/usr/bin/env python3
"""Capture participant registration screenshots (EN/JA/ZH) matching Korean manual."""
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KO_SCRIPT = os.path.join(ROOT, "scripts", "capture_manual_ko.py")

spec = importlib.util.spec_from_file_location("cap_ko", KO_SCRIPT)
cap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cap)

LANGS = {
    "en": {
        "lang_row": "English",
        "suffix": "en",
        "home_marker": "This Month Total",
        "menu_meeting": "Register Event",
        "store_name": "Venue",
        "save_and_add": "Save & Add Participants",
        "add": "Add",
        "name": "Name",
        "gender_female": "Female",
        "gender_male": "Male",
        "count_marker": "4 (M 2 · F 2)",
        "home_checks": (
            lambda t: "Edit Event" not in t and "Basic Info" not in t,
            lambda t: "This Month Total" in t and "Menu" in t,
        ),
        "not_home": (
            "Total (Receipt)",
            "Amount Calculation",
        ),
    },
    "ja": {
        "lang_row": "日本語",
        "suffix": "ja",
        "home_marker": "今月の精算合計",
        "menu_meeting": "集まり情報登録",
        "store_name": "店舗名",
        "save_and_add": "登録して参加者を追加",
        "add": "追加",
        "name": "名前",
        "gender_female": "女性",
        "gender_male": "男性",
        "count_marker": "4名 (男 2 · 女 2)",
        "home_checks": (
            lambda t: "集まり情報の編集" not in t and "基本情報" not in t,
            lambda t: "今月の精算合計" in t and "メニュー" in t,
        ),
        "not_home": (
            "精算金額 (レシート合計)",
            "金額計算",
        ),
    },
    "zh": {
        "lang_row": "中文",
        "suffix": "zh",
        "home_marker": "本月结算合计",
        "menu_meeting": "聚会信息登记",
        "store_name": "店铺名称",
        "save_and_add": "登记并添加参与者",
        "add": "添加",
        "name": "姓名",
        "gender_female": "女性",
        "gender_male": "男性",
        "count_marker": "4人 (男 2 · 女 2)",
        "home_checks": (
            lambda t: "编辑聚会信息" not in t and "基本信息" not in t,
            lambda t: "本月结算合计" in t and "菜单" in t,
        ),
        "not_home": (
            "结算金额 (收据合计)",
            "金额计算",
        ),
    },
}


def configure(lang_key):
    cfg = LANGS[lang_key]

    def is_home():
        t = cap.all_text()
        if not cfg["home_checks"][0](t):
            return False
        if cfg["not_home"][0] in t and cfg["not_home"][1] in t:
            return False
        return cfg["home_checks"][1](t)

    cap.is_home = is_home
    cap.KO = {
        "home_marker": cfg["home_marker"],
        "menu_meeting": cfg["menu_meeting"],
        "menu_monthly": "",
        "menu_settings": "",
        "store_name": cfg["store_name"],
        "save_and_add": cfg["save_and_add"],
        "add": cfg["add"],
        "name": cfg["name"],
        "gender_female": cfg["gender_female"],
        "gender_male": cfg["gender_male"],
        "settlement_title": "",
        "settlement_amount": "",
        "participant_settlement": "",
    }

    def select_gender(male=True):
        label = cfg["gender_male"] if male else cfg["gender_female"]
        for text, desc, cls, clickable, bounds, c in cap.visible_nodes():
            if text == label and clickable == "true":
                cap.tap_xy(*c)
                return
        cap.tap_row(label, exact=True)

    cap.select_gender = select_gender
    return cfg


def open_settings():
    for label in ("Settings", "설정", "設定", "设置"):
        if label in cap.all_text():
            cap.tap_menu(label)
            cap.sleep(2)
            return
    raise RuntimeError("Settings menu not found")


def set_language(cfg):
    cap.clear_app()
    cap.launch(force_stop=True, cold=True)
    open_settings()
    cap.tap_row(cfg["lang_row"], exact=True)
    cap.sleep(1.5)
    cap.sh("shell", "input", "keyevent", "4")
    cap.sleep(1.2)
    if not cap.wait_text(cfg["home_marker"]):
        cap.launch(force_stop=True, cold=False)
    if not cap.wait_text(cfg["home_marker"]):
        raise RuntimeError(f"{cfg['lang_row']} language not applied")


def setup_and_capture(cfg):
    cap.tap_menu(cfg["menu_meeting"])
    cap.sleep(3)
    cap.wait_text(cfg["store_name"])
    cap.tap_field(cfg["store_name"])
    cap.text("Sakura")
    cap.hide_keyboard()
    cap.fling_down()
    cap.fling_down()
    cap.tap_row(cfg["save_and_add"])
    cap.sleep(3)
    cap.wait_text(cfg["add"])
    cap.add_participant("Kim", female=False)
    cap.add_participant("Lee", female=False)
    cap.add_participant("Park", female=True)
    cap.add_participant("Choi", female=True)
    if "4 / 30" not in cap.all_text():
        raise RuntimeError(f"Failed to register 4 participants: {cap.all_text()[:200]!r}")
    if cfg["count_marker"] not in cap.all_text():
        raise RuntimeError(f"Gender count mismatch: {cap.all_text()[:300]!r}")
    cap.hide_keyboard()
    cap.scroll_page()
    cap.scroll_page()
    t = cap.all_text()
    for name in ("Park", "Choi"):
        if name not in t:
            raise RuntimeError(f"Participant {name} not visible after scroll: {t[:300]!r}")
    cap.shot(f"03_participants_{cfg['suffix']}")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    for lang_key in ("en", "ja", "zh"):
        print(f"=== Participants ({lang_key}) ===")
        cfg = configure(lang_key)
        set_language(cfg)
        setup_and_capture(cfg)
    print("=== Done ===")


if __name__ == "__main__":
    main()
