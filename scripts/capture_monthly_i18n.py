#!/usr/bin/env python3
"""Capture monthly summary screenshots (EN/JA/ZH) matching Korean manual."""
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
        "menu_monthly": "Monthly Summary",
        "monthly_title": "Monthly Summary",
        "grand_total": "Grand Total",
        "month_marker": "June 2026",
        "meeting_badge": "1 events",
        "participants_badge": "4 people",
        "zero_amount": "0 KRW",
        "store_name": "Venue",
        "save_and_add": "Save & Add Participants",
        "add": "Add",
        "name": "Name",
        "gender": "Gender",
        "payment_type": "Payment",
        "gender_female": "Female",
        "gender_male": "Male",
        "cash": "Cash",
    },
    "ja": {
        "lang_row": "日本語",
        "suffix": "ja",
        "home_marker": "今月の精算合計",
        "menu_meeting": "集まり情報登録",
        "menu_monthly": "月別精算一覧",
        "monthly_title": "月別精算一覧",
        "grand_total": "全体の精算合計",
        "month_marker": "2026年6月",
        "meeting_badge": "集まり 1件",
        "participants_badge": "参加 4名",
        "zero_amount": "0円",
        "store_name": "店舗名",
        "save_and_add": "登録して参加者を追加",
        "add": "追加",
        "name": "名前",
        "gender": "性別",
        "payment_type": "精算方法",
        "gender_female": "女性",
        "gender_male": "男性",
        "cash": "現金",
    },
    "zh": {
        "lang_row": "中文",
        "suffix": "zh",
        "home_marker": "本月结算合计",
        "menu_meeting": "聚会信息登记",
        "menu_monthly": "月度结算一览",
        "monthly_title": "月度结算一览",
        "grand_total": "全部结算合计",
        "month_marker": "2026年6月",
        "meeting_badge": "1次聚会",
        "participants_badge": "4人",
        "zero_amount": "0韩元",
        "store_name": "店铺名称",
        "save_and_add": "登记并添加参与者",
        "add": "添加",
        "name": "姓名",
        "gender": "性别",
        "payment_type": "结算方式",
        "gender_female": "女性",
        "gender_male": "男性",
        "cash": "现金",
    },
}


def configure(cfg):
    def is_home():
        t = cap.all_text()
        if cfg["monthly_title"] in t and cfg["grand_total"] in t:
            return False
        if cfg["home_marker"] in t and (
            "Menu" in t or "メニュー" in t or "菜单" in t
        ):
            return True
        if cfg["home_marker"] in t and "Recent" in t:
            return True
        if cfg["home_marker"] in t and "最近" in t:
            return True
        return False

    cap.is_home = is_home
    cap.KO = {
        "home_marker": cfg["home_marker"],
        "menu_meeting": cfg["menu_meeting"],
        "menu_monthly": cfg["menu_monthly"],
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

    def select_in_add_form(label, section="payment"):
        if section == "gender":
            top_y = cap.find_label_y(cfg["gender"], exact=True)
            bottom_y = cap.find_label_y(cfg["payment_type"], exact=True)
        else:
            top_y = cap.find_label_y(cfg["payment_type"], exact=True)
            bottom_y = cap.find_label_y(cfg["add"], exact=True)
        if top_y is None or bottom_y is None:
            raise RuntimeError(f"Add form section not found for {label}")
        y_lo = min(top_y, bottom_y) - 10
        y_hi = max(top_y, bottom_y) + 40
        for text, desc, cls, clickable, bounds, c in cap.visible_nodes():
            if text == label and clickable == "true" and y_lo <= c[1] <= y_hi:
                cap.tap_xy(*c)
                return
        raise RuntimeError(f"{label} not found in add form")

    def select_gender(male=True):
        label = cfg["gender_male"] if male else cfg["gender_female"]
        select_in_add_form(label, section="gender")

    def select_cash():
        select_in_add_form(cfg["cash"], section="payment")

    def add_participant(name, female=False):
        cap.tap_field(cfg["name"])
        cap.clear_name_field()
        cap.text(name)
        cap.hide_keyboard()
        select_gender(male=not female)
        select_cash()
        cap.tap_add_button()
        cap.sleep(1.0)

    cap.select_gender = select_gender
    cap.add_participant = add_participant


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


def setup_meeting(cfg):
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
        raise RuntimeError(f"Failed to register participants: {cap.all_text()[:200]!r}")


def capture_monthly(cfg):
    cap.force_home()
    cap.tap_menu(cfg["menu_monthly"])
    cap.sleep(2)
    if not cap.wait_text(cfg["monthly_title"], timeout=10):
        raise RuntimeError(f"Monthly screen not open: {cap.all_text()[:300]!r}")
    t = cap.all_text()
    for marker in (
        cfg["grand_total"],
        cfg["month_marker"],
        cfg["meeting_badge"],
        cfg["participants_badge"],
        cfg["zero_amount"],
    ):
        if marker not in t:
            raise RuntimeError(f"Missing {marker!r} on monthly screen: {t[:400]!r}")
    cap.hide_keyboard()
    cap.shot(f"07_monthly_{cfg['suffix']}")
    if cfg["monthly_title"] not in cap.all_text():
        raise RuntimeError("Screenshot taken but monthly screen markers missing")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    langs = sys.argv[1:] if len(sys.argv) > 1 else ("en", "ja", "zh")
    for lang_key in langs:
        cfg = LANGS[lang_key]
        print(f"=== Monthly ({lang_key}) ===")
        configure(cfg)
        set_language(cfg)
        setup_meeting(cfg)
        capture_monthly(cfg)
    print("=== Done ===")


if __name__ == "__main__":
    main()
