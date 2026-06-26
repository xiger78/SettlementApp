#!/usr/bin/env python3
"""Capture participant settlement list screenshots (EN/JA/ZH) matching Korean manual."""
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
        "gender": "Gender",
        "payment_type": "Payment",
        "cash": "Cash",
        "paypay": "PayPay",
        "settlement_title": "Settlement",
        "settlement_amount": "Total (Receipt)",
        "participant_settlement": "Participant Settlement",
        "receipt_hint": "Take a photo of the receipt",
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
        "gender": "性別",
        "payment_type": "精算方法",
        "cash": "現金",
        "paypay": "PayPay",
        "settlement_title": "精算",
        "settlement_amount": "精算金額 (レシート合計)",
        "participant_settlement": "参加者の精算",
        "receipt_hint": "カメラでレシートを撮影してください",
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
        "gender": "性别",
        "payment_type": "结算方式",
        "cash": "现金",
        "paypay": "PayPay",
        "settlement_title": "结算",
        "settlement_amount": "结算金额 (收据合计)",
        "participant_settlement": "参与者结算",
        "receipt_hint": "请用相机拍摄收据",
    },
}


def configure(cfg):
    def is_home():
        t = cap.all_text()
        if cfg["settlement_amount"] in t and "金额计算" in t or cfg["settlement_amount"] in t and "金額計算" in t:
            return False
        if cfg["settlement_amount"] in t and "Amount Calculation" in t:
            return False
        return cfg["home_marker"] in t

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
        "settlement_title": cfg["settlement_title"],
        "settlement_amount": cfg["settlement_amount"],
        "participant_settlement": cfg["participant_settlement"],
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


def setup_and_open_settlement(cfg):
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
    cap.sleep(2)
    cap.open_settlement()


def safe_scroll():
    cap.dismiss_keyboard()
    cap.sh("shell", "input", "swipe", "50", "1500", "50", "650", "400")
    cap.sleep(0.8)


def ensure_all_cash(cfg):
    for name in ("Kim", "Lee", "Park", "Choi"):
        name_y = None
        for text, desc, cls, clickable, bounds, c in cap.visible_nodes():
            if text == name:
                name_y = c[1]
                break
        if name_y is None:
            continue
        for text, desc, cls, clickable, bounds, c in cap.visible_nodes():
            if text == cfg["paypay"] and clickable == "true" and abs(c[1] - name_y) <= 60:
                cap.tap_xy(*c)
                cap.sleep(0.4)
                break


def apply_amounts_and_capture_list(cfg):
    cap.enter_settlement_amount(0, 40000)
    cap.wait_text("10,000", timeout=5)
    cap.enter_settlement_amount(1, 5000)
    cap.wait_text("7,500", timeout=5)
    cap.scroll_to_top()
    cap.enter_settlement_amount(2, 5000)
    cap.wait_text("15,000", timeout=5)
    cap.sleep(1.5)
    cap.dismiss_keyboard()
    cap.tap_xy(540, 400)
    for _ in range(12):
        t = cap.all_text()
        if (
            "Kim" in t
            and "Choi" in t
            and cfg["participant_settlement"] in t
            and t.count("15,000") >= 2
            and t.count("5,000") >= 2
        ):
            break
        safe_scroll()
    else:
        raise RuntimeError(f"Participant list not ready: {cap.all_text()[:400]!r}")
    ensure_all_cash(cfg)
    cap.shot(f"05_settlement_list_{cfg['suffix']}")


def main():
    if not os.path.exists(cap.ADB):
        sys.exit(f"adb not found: {cap.ADB}")
    devices = cap.sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(cap.SHOT_DIR, exist_ok=True)
    langs = sys.argv[1:] if len(sys.argv) > 1 else ("en", "ja", "zh")
    for lang_key in langs:
        print(f"=== Settlement list ({lang_key}) ===")
        cfg = configure(LANGS[lang_key])
        set_language(cfg)
        setup_and_open_settlement(cfg)
        apply_amounts_and_capture_list(cfg)
    print("=== Done ===")


if __name__ == "__main__":
    main()
