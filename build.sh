#!/usr/bin/env bash
# 无 Gradle 的手工打包脚本：aapt2 -> javac -> d8 -> apksigner
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP="$ROOT/app/src/main"
OUT="$ROOT/app/build/outputs/apk"
WORK="$ROOT/app/build/tmp"

: "${ANDROID_SDK:?请先设置 ANDROID_SDK 环境变量（指向 Android SDK 根目录）}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_SDK/platforms/android-34/android.jar}"
BUILD_TOOLS="${BUILD_TOOLS:-$ANDROID_SDK/build-tools/34.0.0}"
PKG="com.doubao.calendar2026"
APK_NAME="2026日历.apk"

echo "==> 清理"
rm -rf "$WORK" "$OUT"
mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/dex" "$OUT"

echo "==> 1/6 编译资源 (aapt2 compile)"
"$BUILD_TOOLS/aapt2" compile --dir "$APP/res" -o "$WORK/res.zip"

echo "==> 2/6 生成含 package 的清单（aapt2 link 需要）"
python3 - "$APP/AndroidManifest.xml" "$WORK/AndroidManifest.xml" "$PKG" <<'PY'
import sys
src, dst, pkg = sys.argv[1], sys.argv[2], sys.argv[3]
s = open(src, encoding='utf-8').read()
s = s.replace('<manifest xmlns:android="http://schemas.android.com/apk/res/android">',
              '<manifest xmlns:android="http://schemas.android.com/apk/res/android"\n    package="%s">' % pkg, 1)
open(dst, 'w', encoding='utf-8').write(s)
PY

echo "==> 3/6 链接资源并生成 R.java (aapt2 link)"
"$BUILD_TOOLS/aapt2" link \
  -o "$WORK/base.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$WORK/AndroidManifest.xml" \
  --min-sdk-version 21 \
  --target-sdk-version 34 \
  --version-code 1 --version-name "1.0" \
  --java "$WORK/gen" \
  --auto-add-overlay \
  "$WORK/res.zip"

echo "==> 4/6 编译 Java (javac)"
find "$APP/java" "$WORK/gen" -name '*.java' > "$WORK/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -nowarn -Xlint:-options \
  -bootclasspath "$ANDROID_JAR" \
  -classpath "$ANDROID_JAR" \
  -d "$WORK/classes" \
  @"$WORK/sources.txt"

echo "==> 5/6 生成 dex (d8)"
D8_BIN="$BUILD_TOOLS/d8"
"$D8_BIN" \
  --lib "$ANDROID_JAR" --min-api 21 --release \
  --output "$WORK/dex" \
  $(find "$WORK/classes" -name '*.class')

echo "==> 6/6 打包 + 对齐 + 签名"
python3 - "$WORK/base.apk" "$WORK/dex" "$WORK/unaligned.apk" <<'PY'
import os, sys, zipfile
apk, dexdir, dst = sys.argv[1], sys.argv[2], sys.argv[3]
src = zipfile.ZipFile(apk)
out = zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED)
for item in src.infolist():
    out.writestr(item, src.read(item.filename))
for f in os.listdir(dexdir):
    out.write(os.path.join(dexdir, f), f)
out.close()
PY

"$BUILD_TOOLS/zipalign" -f 4 "$WORK/unaligned.apk" "$WORK/aligned.apk"

KS="$ROOT/debug.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
fi

APKSIGNER="$BUILD_TOOLS/apksigner"
"$APKSIGNER" sign \
  --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out "$OUT/$APK_NAME" "$WORK/aligned.apk"

"$APKSIGNER" verify --print-certs "$OUT/$APK_NAME" | head -4
echo "==> 完成: $OUT/$APK_NAME"
ls -lh "$OUT/$APK_NAME"
