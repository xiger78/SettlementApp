#!/usr/bin/env python3
"""Capture Korean manual screenshots via Android emulator."""
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

ADB = os.environ.get(
    "ADB",
    os.path.expanduser("~/Library/Android/sdk/platform-tools/adb"),
)
PKG = "com.example.settlementapp"
ACTIVITY = f"{PKG}/.MainActivity"
SHOT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs",
    "screenshots",
)

KO = {
    "home_marker": "이번 달 정산 합계",
    "menu_meeting": "모임정보등록",
    "menu_monthly": "월별정산일람",
    "menu_settings": "설정",
    "store_name": "가게이름",
    "save_and_add": "등록하고 참가자 추가",
    "add": "추가",
    "name": "이름",
    "gender_female": "여자",
    "settlement_title": "정산",
    "settlement_amount": "정산금액 (영수증 총액)",
    "gender_diff": "남여차이 금액",
    "female_amount": "여자 1인 금액",
    "participant_settlement": "참가자 정산",
}


def sh(*args):
    return subprocess.run([ADB, *args], capture_output=True, text=True)


def sleep(sec):
    time.sleep(sec)


def get_xml():
    sh("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    out = subprocess.run(
        [ADB, "shell", "cat", "/sdcard/ui.xml"], capture_output=True, text=True
    ).stdout
    i = out.find("<?xml")
    return out[i:] if i > 0 else out


def center(bounds):
    m = re.findall(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m[0])
    if x2 <= x1 or y2 <= y1:
        return None
    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
    return None if cx <= 0 and cy <= 0 else (cx, cy)


def nodes():
    root = ET.fromstring(get_xml())
    return [
        (
            n.get("text", ""),
            n.get("content-desc", ""),
            n.get("class", ""),
            n.get("clickable", ""),
            n.get("bounds", ""),
        )
        for n in root.iter("node")
    ]


def visible_nodes():
    out = []
    for text, desc, cls, clickable, bounds in nodes():
        c = center(bounds)
        if c and c[1] > 80:
            out.append((text, desc, cls, clickable, bounds, c))
    return out


def all_text():
    parts = []
    for text, desc, *_ in visible_nodes():
        if text:
            parts.append(text)
        if desc:
            parts.append(desc)
    return "\n".join(parts)


def wait_text(query, timeout=15):
    for _ in range(timeout * 2):
        if query in all_text():
            return True
        sleep(0.5)
    return False


def in_app():
    out = subprocess.run(
        [ADB, "shell", "dumpsys", "window"], capture_output=True, text=True
    ).stdout
    return PKG in out


def launch(force_stop=False, cold=True):
    if force_stop:
        sh("shell", "am", "force-stop", PKG)
        sleep(0.8)
    sh("shell", "am", "start", "-n", ACTIVITY)
    sleep(18 if cold else 5)


def clear_app():
    sh("shell", "am", "force-stop", PKG)
    sleep(0.5)
    sh("shell", "pm", "clear", PKG)
    sleep(1)


def tap_xy(x, y):
    sh("shell", "input", "tap", str(x), str(y))
    sleep(0.8)


def text(value):
    sh("shell", "input", "text", value.replace(" ", "%s"))
    sleep(0.5)


def hide_keyboard():
    sh("shell", "input", "keyevent", "111")
    sleep(0.5)


def fling_down():
    sh("shell", "input", "swipe", "540", "1700", "540", "350", "250")
    sleep(1.0)


def find_label_y(label, exact=True):
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        for val in (text, desc):
            if not val:
                continue
            ok = val == label if exact else label in val
            if ok:
                return c[1]
    return None


def tap_menu(label):
    ty = find_label_y(label)
    if ty is None:
        raise RuntimeError(f"Menu not found: {label}")
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if clickable == "true" and abs(c[1] - ty) <= 90 and c[0] > 300:
            tap_xy(*c)
            return
    raise RuntimeError(f"Clickable menu not found: {label}")


def tap_row(label, exact=True):
    ty = find_label_y(label, exact=exact)
    if ty is None:
        raise RuntimeError(f"Row not found: {label}")
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if clickable == "true" and abs(c[1] - ty) <= 60:
            tap_xy(*c)
            return
    tap_xy(540, ty)


def tap_field(label):
    ty = find_label_y(label, exact=True)
    if ty is None:
        raise RuntimeError(f"Field label not found: {label}")
    best = None
    best_dy = 9999
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if "EditText" not in cls or clickable != "true":
            continue
        dy = abs(c[1] - ty)
        if dy <= 120 and dy < best_dy:
            best = c
            best_dy = dy
    if not best:
        raise RuntimeError(f"EditText not found for: {label}")
    tap_xy(*best)


def dismiss_keyboard():
    hide_keyboard()
    tap_xy(990, 1750)
    sleep(0.4)
    hide_keyboard()


def is_home():
    t = all_text()
    if "모임정보 수정" in t or "기본 정보" in t:
        return False
    if "참가자 추가" in t and "참가자 명단" in t:
        return False
    if "정산금액 (영수증 총액)" in t:
        return False
    if KO["home_marker"] in t and "메뉴" in t:
        return True
    if "최근 모임" in t and "설정" in t:
        return True
    return False


def force_home():
    for _ in range(6):
        if is_home():
            return
        sh("shell", "input", "keyevent", "4")
        sleep(1.0)
    sh("shell", "am", "force-stop", PKG)
    sleep(0.5)
    sh("shell", "am", "start", "-n", ACTIVITY)
    sleep(8)
    if not is_home():
        raise RuntimeError("Could not navigate to home")


def shot(name):
    hide_keyboard()
    if not in_app():
        raise RuntimeError(f"App not focused: {name}")
    path = os.path.join(SHOT_DIR, f"{name}.png")
    sh("shell", "screencap", "-p", "/sdcard/s.png")
    sh("pull", "/sdcard/s.png", path)
    size = os.path.getsize(path)
    print(f"  SHOT {name}.png ({size:,} bytes)")
    if size > 400_000:
        raise RuntimeError(f"Screenshot too large (launcher?): {name}")


def select_gender(male=True):
    label = "남자" if male else KO["gender_female"]
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if text == label and clickable == "true":
            tap_xy(*c)
            return
    tap_row(label, exact=True)


def scroll_page():
    dismiss_keyboard()
    sh("shell", "input", "swipe", "540", "1400", "540", "700", "500")
    sleep(0.8)


def set_korean():
    launch(force_stop=True, cold=True)
    if not wait_text("Settings"):
        raise RuntimeError("Expected English UI after pm clear")
    tap_menu("Settings")
    sleep(2)
    if "system language" not in all_text():
        raise RuntimeError("Settings screen not open")
    tap_row("한국어", exact=True)
    sleep(1.5)
    sh("shell", "input", "keyevent", "4")
    sleep(1.2)
    if not wait_text(KO["home_marker"]):
        launch(force_stop=True, cold=False)
    if not wait_text(KO["home_marker"]):
        raise RuntimeError("Korean language not applied")


def clear_name_field():
    hide_keyboard()
    sh("shell", "input", "keyevent", "123")
    for _ in range(10):
        sh("shell", "input", "keyevent", "67")
        sleep(0.05)
    sleep(0.2)


def tap_add_button():
    ty = find_label_y(KO["add"], exact=True)
    if ty is None:
        raise RuntimeError("Add button not found")
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if clickable == "true" and abs(c[1] - ty) <= 60 and c[0] > 200:
            tap_xy(*c)
            return
    tap_xy(540, ty)


def add_participant(name, female=False):
    tap_field(KO["name"])
    clear_name_field()
    text(name)
    hide_keyboard()
    select_gender(male=not female)
    tap_add_button()
    sleep(1.0)


def open_settlement():
    for text_val, desc, cls, clickable, bounds, c in visible_nodes():
        if text_val == KO["settlement_title"] or desc == KO["settlement_title"]:
            tap_xy(*c)
            sleep(2)
            if wait_text(KO["settlement_amount"], timeout=8):
                return
    tap_xy(1000, 147)
    sleep(2)
    if not wait_text(KO["settlement_amount"]):
        raise RuntimeError("Settlement screen not opened")


def scroll_until(text, max_scrolls=12):
    dismiss_keyboard()
    for _ in range(max_scrolls):
        if text in all_text():
            return True
        scroll_page()
    return text in all_text()


def enter_amount(label, amount):
    hide_keyboard()
    tap_field(label)
    sleep(0.4)
    text(str(amount))
    hide_keyboard()
    tap_xy(540, 400)
    sleep(0.6)


def setup_data():
    tap_menu(KO["menu_meeting"])
    sleep(3)
    wait_text(KO["store_name"])
    tap_field(KO["store_name"])
    text("Sakura")
    hide_keyboard()
    shot("02_meetingform_ko")

    fling_down()
    fling_down()
    tap_row(KO["save_and_add"])
    sleep(3)
    wait_text(KO["add"])
    add_participant("Kim", female=False)
    add_participant("Lee", female=False)
    add_participant("Park", female=True)
    add_participant("Choi", female=True)
    if "4 / 30" not in all_text():
        raise RuntimeError(f"Failed to register 4 participants: {all_text()[:200]!r}")
    scroll_page()
    scroll_page()
    shot("03_participants_ko")

    sleep(2)
    open_settlement()
    if "Kim" not in all_text() and "4명" not in all_text():
        scroll_until("Kim", max_scrolls=4)


def settlement_fields():
    fields = []
    for text, desc, cls, clickable, bounds, c in visible_nodes():
        if "EditText" in cls and clickable == "true" and c[1] > 700:
            fields.append(c)
    fields.sort(key=lambda p: p[1])
    return fields


def enter_settlement_amount(index, amount):
    dismiss_keyboard()
    tap_xy(540, 400)
    sleep(0.5)
    fields = settlement_fields()
    if index >= len(fields):
        raise RuntimeError(f"Need field {index}, found {len(fields)}: {fields}")
    tap_xy(*fields[index])
    sleep(0.4)
    sh("shell", "input", "keyevent", "123")
    for _ in range(8):
        sh("shell", "input", "keyevent", "67")
        sleep(0.05)
    text(str(amount))
    dismiss_keyboard()
    sleep(0.5)


def scroll_to_top():
    for _ in range(6):
        sh("shell", "input", "swipe", "540", "650", "540", "1600", "400")
        sleep(0.6)


def capture_settlement():
    enter_settlement_amount(0, 40000)
    wait_text("10,000", timeout=5)
    shot("04_settlement_calc_ko")

    enter_settlement_amount(1, 5000)
    wait_text("7,500", timeout=5)
    shot("04_settlement_genderdiff_ko")

    scroll_to_top()
    enter_settlement_amount(2, 5000)
    wait_text("15,000", timeout=5)
    shot("04_settlement_split_ko")

    dismiss_keyboard()
    tap_xy(540, 400)
    for _ in range(10):
        if "Kim" in all_text() and KO["participant_settlement"] in all_text():
            break
        scroll_page()
    shot("05_settlement_list_ko")


def capture_home_monthly_settings():
    force_home()
    shot("01_home_ko")

    tap_menu(KO["menu_monthly"])
    sleep(2)
    wait_text(KO["menu_monthly"])
    shot("07_monthly_ko")

    sh("shell", "input", "keyevent", "4")
    sleep(1.5)
    force_home()
    tap_menu(KO["menu_settings"])
    sleep(2)
    wait_text("한국어")
    shot("08_settings_ko")


def main():
    if not os.path.exists(ADB):
        print(f"adb not found: {ADB}", file=sys.stderr)
        sys.exit(1)
    devices = sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        print("No emulator/device connected", file=sys.stderr)
        sys.exit(1)

    os.makedirs(SHOT_DIR, exist_ok=True)
    print("=== Korean manual screenshots ===")
    clear_app()
    set_korean()
    setup_data()
    capture_settlement()
    capture_home_monthly_settings()
    print("=== Done ===")


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "home-only":
        os.makedirs(SHOT_DIR, exist_ok=True)
        capture_home_monthly_settings()
        print("=== Done (home-only) ===")
    else:
        main()
