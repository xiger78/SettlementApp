#!/usr/bin/env python3
"""Capture settings (language) screenshots for JA/ZH matching Korean manual."""
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KO_SCRIPT = os.path.join(ROOT, "scripts", "capture_manual_ko.py")

spec = importlib.util.spec_from_file_location("cap_ko", KO_SCRIPT)
cap = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cap)

LANGS = {
    "ja": {
        "lang_row": "日本語",
        "suffix": "ja",
        "home_marker": "今月の精算合計",
        "menu_settings": "設定",
        "settings_title": "設定",
        "language_setting": "言語設定",
        "app_name": "精算アプリ",
    },
    "zh": {
        "lang_row": "中文",
        "suffix": "zh",
        "home_marker": "本月结算合计",
        "menu_settings": "设置",
        "settings_title": "设置",
        "language_setting": "语言设置",
        "app_name": "结算应用",
    },
}


def configure(cfg):
    def is_home():
        t = cap.all_text()
        if cfg["settings_title"] in t and cfg["language_setting"] in t:
            return False
        if cfg["home_marker"] in t and ("メニュー" in t or "菜单" in t):
            return True
        if cfg["home_marker"] in t and "最近" in t:
            return True
        return False

    cap.is_home = is_home
    cap.KO = {
        "home_marker": cfg["home_marker"],
        "menu_meeting": "",
        "menu_monthly": "",
        "menu_settings": cfg["menu_settings"],
        "store_name": "",
        "save_and_add": "",
        "add": "",
        "name": "",
        "gender_female": "",
        "gender_male": "",
        "settlement_title": "",
        "settlement_amount": "",
        "participant_settlement": "",
    }


def open_settings_menu():
    for label in ("Settings", "설정", "設定", "设置"):
        if label in cap.all_text():
            cap.tap_menu(label)
            cap.sleep(2)
            return
    raise RuntimeError("Settings menu not found")


def apply_language(cfg):
    cap.clear_app()
    cap.launch(force_stop=True, cold=True)
    open_settings_menu()
    cap.tap_row(cfg["lang_row"], exact=True)
    cap.sleep(1.5)
    cap.sh("shell", "input", "keyevent", "4")
    cap.sleep(1.2)
    cap.force_home()
    if cfg["home_marker"] not in cap.all_text():
        raise RuntimeError(f"{cfg['lang_row']} not applied: {cap.all_text()[:300]!r}")


def capture_settings(cfg):
    cap.tap_menu(cfg["menu_settings"])
    cap.sleep(2)
    if not cap.wait_text(cfg["language_setting"], timeout=8):
        raise RuntimeError(f"Settings screen not open: {cap.all_text()[:300]!r}")
    t = cap.all_text()
    for label in ("한국어", "日本語", "English", "中文"):
        if label not in t:
            raise RuntimeError(f"Missing language option {label}: {t[:400]!r}")
    if cfg["lang_row"] not in t:
        raise RuntimeError(f"Selected language {cfg['lang_row']!r} not visible")
    cap.hide_keyboard()
    cap.shot(f"08_settings_{cfg['suffix']}")
    if cfg["language_setting"] not in cap.all_text():
        raise RuntimeError("Screenshot taken but settings screen markers missing")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    langs = sys.argv[1:] if len(sys.argv) > 1 else ("ja", "zh")
    for lang_key in langs:
        cfg = LANGS[lang_key]
        print(f"=== Settings ({lang_key}) ===")
        configure(cfg)
        apply_language(cfg)
        capture_settings(cfg)
    print("=== Done ===")


if __name__ == "__main__":
    main()
