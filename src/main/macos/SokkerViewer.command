#!/bin/sh
# starts SokkerViewer from the folder this script is in
cd "$(dirname "$0")" || exit 1
# removes macOS's download mark while java still carries it
if xattr -p com.apple.quarantine runtime/bin/java >/dev/null 2>&1; then
	xattr -dr com.apple.quarantine . 2>/dev/null
fi
mkdir -p tmp
nohup runtime/bin/java -XstartOnFirstThread -Xdock:name=SokkerViewer -Xdock:icon=SokkerViewer.png -jar Launcher.jar >tmp/start.log 2>&1 &
