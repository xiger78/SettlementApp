#!/usr/bin/env python3
"""Capture Japanese home screen for MANUAL_ja.md."""
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

JA = {
    "home_marker": "今月の精算合計",
    "menu": "メニュー",
    "app_name": "精算アプリ",
    "menu_meeting": "集まり情報登録",
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


def visible_nodes():
    root = ET.fromstring(get_xml())
    out = []
    for n in root.iter("node"):
        c = center(n.get("bounds", ""))
        if c and c[1] > 80:
            out.append(
                (
                    n.get("text", ""),
                    n.get("content-desc", ""),
                    n.get("clickable", ""),
                    c,
                )
            )
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


def find_label_y(label):
    for text, desc, clickable, c in visible_nodes():
        for val in (text, desc):
            if val == label:
                return c[1]
    return None


def tap_menu(label):
    ty = find_label_y(label)
    if ty is None:
        raise RuntimeError(f"Menu not found: {label}")
    for text, desc, clickable, c in visible_nodes():
        if clickable == "true" and abs(c[1] - ty) <= 90 and c[0] > 300:
            sh("shell", "input", "tap", str(c[0]), str(c[1]))
            sleep(0.8)
            return
    raise RuntimeError(f"Clickable menu not found: {label}")


def tap_row(label):
    ty = find_label_y(label)
    if ty is None:
        raise RuntimeError(f"Row not found: {label}")
    for text, desc, clickable, c in visible_nodes():
        if clickable == "true" and abs(c[1] - ty) <= 60:
            sh("shell", "input", "tap", str(c[0]), str(c[1]))
            sleep(0.8)
            return
    sh("shell", "input", "tap", "540", str(ty))
    sleep(0.8)


def is_home():
    t = all_text()
    if "集まり情報の編集" in t or "基本情報" in t:
        return False
    if "精算金額" in t:
        return False
    if JA["home_marker"] in t and JA["menu"] in t:
        return True
    if "最近の集まり" in t and "設定" in t:
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
        raise RuntimeError(f"Could not reach home: {all_text()[:200]!r}")


def open_settings():
    for label in ("Settings", "설정", "設定", "设置"):
        if label in all_text():
            tap_menu(label)
            sleep(2)
            return
    raise RuntimeError("Settings menu not found")


def set_japanese():
    sh("shell", "am", "force-stop", PKG)
    sleep(0.5)
    sh("shell", "am", "start", "-n", ACTIVITY)
    sleep(18)
    open_settings()
    if not wait_text("日本語", timeout=5) and "Language" not in all_text() and "言語" not in all_text():
        raise RuntimeError("Settings screen not open")
    tap_row("日本語")
    sleep(1.5)
    sh("shell", "input", "keyevent", "4")
    sleep(1.2)
    force_home()
    if JA["app_name"] not in all_text():
        raise RuntimeError(f"Japanese not applied: {all_text()[:300]!r}")


def shot():
    sh("shell", "input", "keyevent", "111")
    sleep(0.4)
    path = os.path.join(SHOT_DIR, "01_home_ja.png")
    sh("shell", "screencap", "-p", "/sdcard/s.png")
    sh("pull", "/sdcard/s.png", path)
    size = os.path.getsize(path)
    print(f"SHOT 01_home_ja.png ({size:,} bytes)")
    if JA["app_name"] not in all_text() and JA["home_marker"] not in all_text():
        raise RuntimeError("Screenshot taken but Japanese home markers missing")


def main():
    if not os.path.exists(ADB):
        sys.exit(f"adb not found: {ADB}")
    devices = sh("devices").stdout.strip().splitlines()
    if len(devices) < 2 or "device" not in devices[1]:
        sys.exit("No emulator/device connected")
    os.makedirs(SHOT_DIR, exist_ok=True)
    set_japanese()
    force_home()
    if JA["menu_meeting"] not in all_text():
        raise RuntimeError(f"Not on Japanese home: {all_text()[:300]!r}")
    shot()
    print("Done.")


if __name__ == "__main__":
    main()
