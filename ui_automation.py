import subprocess
import time
import os
import json
import signal
import xml.etree.ElementTree as ET

# Configuration
CLINIC_LEDGER_PKG = "com.clinicledger"
CHATGPT_PKG = "com.openai.chatgpt"
OUTPUT_DIR = "automation_output"
SCREENSHOT_DIR = os.path.join(OUTPUT_DIR, "screenshots")
VIDEO_DIR = os.path.join(OUTPUT_DIR, "videos")
DATA_FILE = os.path.join(OUTPUT_DIR, "ui_data.json")

# ADB Path - Updated to use full path from your SDK
ADB_PATH = r"C:\Users\gagan\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE_SERIAL = "emulator-5554"

# Displays
PRIMARY_DISPLAY = "0"
SECONDARY_DISPLAY = "2" # Based on your emulator's dumpsys

# Ensure output directories exist
os.makedirs(SCREENSHOT_DIR, exist_ok=True)
os.makedirs(VIDEO_DIR, exist_ok=True)

def run_adb(command, serial=DEVICE_SERIAL):
    """Executes an ADB command and returns the output."""
    try:
        cmd_list = [ADB_PATH]
        if serial:
            cmd_list += ["-s", serial]
        cmd_list += command.split()
        result = subprocess.run(cmd_list, capture_output=True, text=True, check=True)
        return result.stdout.strip()
    except Exception as e:
        print(f"Error running adb {command}: {e}")
        return None

def start_video_recording(display_id, filename):
    """Starts video recording in the background."""
    print(f"Starting video recording on display {display_id}...")
    remote_path = f"/sdcard/video_{display_id}.mp4"
    # --time-limit 0 for unlimited (supported on newer Android)
    cmd = [ADB_PATH, "-s", DEVICE_SERIAL, "shell", "screenrecord", "--display-id", display_id, "--time-limit", "0", remote_path]
    process = subprocess.Popen(cmd)
    return process, remote_path, filename

def stop_video_recording(process, remote_path, local_path):
    """Stops video recording and pulls the file."""
    print(f"Stopping video recording for {remote_path}...")
    # Send SIGINT to screenrecord to stop it cleanly
    # On Windows, we might need a different approach, but try terminating
    process.terminate()
    time.sleep(2) # Give it time to finalize the file
    run_adb(f"pull {remote_path} {local_path}")
    run_adb(f"shell rm {remote_path}")

def launch_app(package, display_id=PRIMARY_DISPLAY):
    print(f"Checking if {package} is installed...")
    packages = run_adb("shell pm list packages")
    if packages and package in packages:
        print(f"Launching {package} on display {display_id}...")
        # Using 'am start' with --display flag
        run_adb(f"shell am start -n {package}/.ui.MainActivity --display {display_id}")
        time.sleep(2)
    else:
        print(f"Warning: Package {package} not found on device.")

def capture_ui_hierarchy():
    """Dumps the UI hierarchy and parses it for useful data."""
    # uiautomator dump usually only captures the default display
    run_adb("shell uiautomator dump /sdcard/view.xml")
    run_adb(f"pull /sdcard/view.xml {OUTPUT_DIR}/view.xml")

    data = []
    try:
        if os.path.exists(f"{OUTPUT_DIR}/view.xml"):
            tree = ET.parse(f"{OUTPUT_DIR}/view.xml")
            root = tree.getroot()
            for node in root.iter('node'):
                text = node.get('text')
                resource_id = node.get('resource-id')
                class_name = node.get('class')
                if text or resource_id:
                    data.append({
                        "text": text,
                        "id": resource_id,
                        "class": class_name,
                        "bounds": node.get('bounds')
                    })
    except Exception as e:
        print(f"Error parsing UI hierarchy: {e}")
    return data

def main_loop(video_processes):
    print("Starting automation loop. Press Ctrl+C to stop.")
    ui_history = []
    count = 0

    try:
        while True:
            timestamp = int(time.time())
            # 1. Capture Screenshots for both displays
            for d_id in [PRIMARY_DISPLAY, SECONDARY_DISPLAY]:
                screenshot_path = os.path.join(SCREENSHOT_DIR, f"screenshot_d{d_id}_{timestamp}.png")
                run_adb(f"shell screencap -d {d_id} -p /sdcard/screen_{d_id}.png")
                run_adb(f"pull /sdcard/screen_{d_id}.png {screenshot_path}")

            # 2. Capture UI Data (Mostly from primary display for now)
            current_ui = capture_ui_hierarchy()
            ui_history.append({
                "timestamp": timestamp,
                "elements": current_ui
            })

            # 3. Save progress
            with open(DATA_FILE, "w", encoding="utf-8") as f:
                json.dump(ui_history, f, indent=4)

            print(f"[{count}] Captured screenshots and UI data at {timestamp}")
            count += 1
            time.sleep(1) # Every second

    except KeyboardInterrupt:
        print("\nAutomation stopped by user.")

if __name__ == "__main__":
    video_records = []
    try:
        # 1. Start video recording
        v1_proc, v1_remote, v1_local = start_video_recording(PRIMARY_DISPLAY, os.path.join(VIDEO_DIR, "primary_display.mp4"))
        v2_proc, v2_remote, v2_local = start_video_recording(SECONDARY_DISPLAY, os.path.join(VIDEO_DIR, "secondary_display.mp4"))
        video_records.append((v1_proc, v1_remote, v1_local))
        video_records.append((v2_proc, v2_remote, v2_local))

        # 2. Launch apps on different displays
        # ChatGPT on secondary, Clinic Ledger on primary
        launch_app(CLINIC_LEDGER_PKG, PRIMARY_DISPLAY)
        launch_app(CHATGPT_PKG, SECONDARY_DISPLAY)

        # 3. Run loop
        main_loop(video_records)

    finally:
        # Cleanup video recording
        for proc, remote, local in video_records:
            stop_video_recording(proc, remote, local)
