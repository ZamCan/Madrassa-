#!/data/data/com.termux/files/usr/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT="$(cd "$SCRIPT_DIR/.." && pwd)"
if [ -n "$ANDROID_HOME" ]; then
    SDK="$ANDROID_HOME"
elif [ -d "/data/data/com.termux/files/home/android-sdk" ]; then
    SDK="/data/data/com.termux/files/home/android-sdk"
else
    SDK="$HOME/android-sdk"
fi
ANDROID_JAR="$SDK/platforms/android-35/android.jar"

BUILD="$PROJECT/manual/v71"
COMPILED="$BUILD/res-compiled"
GEN="$BUILD/generated"
CLASSES="$BUILD/classes"
DEX="$BUILD/dex"
APK="$BUILD/apk"

echo "=========================================="
echo " Madrassa — EduNoor V7.1 MANUAL BUILD"
echo "=========================================="

rm -rf "$BUILD"
mkdir -p "$COMPILED" "$GEN" "$CLASSES" "$DEX" "$APK"

echo
echo "[1/8] Checking toolchain..."

command -v aapt2
command -v d8
command -v javac
command -v apksigner

test -f "$ANDROID_JAR"

echo "android.jar: OK"
echo "Toolchain: OK"

echo
echo "[2/8] Compiling Android resources..."

aapt2 compile \
    --dir "$PROJECT/app/src/main/res" \
    -o "$COMPILED"

echo "Resources compiled."

echo
echo "[3/8] Linking resources + generating R.java..."

aapt2 link \
    -o "$APK/base-unsigned.apk" \
    --manifest "$PROJECT/app/src/main/AndroidManifest.xml" \
    -I "$ANDROID_JAR" \
    --java "$GEN" \
    --min-sdk-version 26 \
    --target-sdk-version 35 \
    --auto-add-overlay \
    "$COMPILED"/*.flat

echo "AAPT2 link: OK"
echo "Generated R.java:"
find "$GEN" -type f -name "R.java" -print

echo
echo "[4/8] Compiling Java sources..."

JAVA_FILES=$(find "$PROJECT/app/src/main/java" \
    -type f \
    -name "*.java" \
    -size +0c \
    | sort)

javac \
    -encoding UTF-8 \
    -source 8 \
    -target 8 \
    -classpath "$ANDROID_JAR" \
    -d "$CLASSES" \
    "$GEN"/com/zamcan/madrassa/R.java \
    $JAVA_FILES

echo "Java compilation: OK"

echo
echo "[5/8] Converting Java bytecode to DEX..."

d8 \
    --lib "$ANDROID_JAR" \
    --min-api 26 \
    --output "$DEX" \
    $(find "$CLASSES" -type f -name "*.class" | sort)

echo "D8: OK"

echo
echo "[6/8] Adding classes.dex to APK..."

python - "$APK/base-unsigned.apk" "$DEX/classes.dex" "$APK/Madrassa-EduNoor-v7.1-unsigned.apk" <<'PY'
import sys
import zipfile

src = sys.argv[1]
dex = sys.argv[2]
dst = sys.argv[3]

with zipfile.ZipFile(src, "r") as zin:
    with zipfile.ZipFile(dst, "w", compression=zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            zout.writestr(item, data)

        with open(dex, "rb") as f:
            data = f.read()

        info = zipfile.ZipInfo("classes.dex")
        info.compress_type = zipfile.ZIP_STORED
        info.external_attr = 0o644 << 16
        zout.writestr(info, data)

print("classes.dex added.")
PY

echo "APK assembled."

echo
echo "[7/8] Aligning APK..."

python "$PROJECT/manual/align_apk.py" \
    "$APK/Madrassa-EduNoor-v7.1-unsigned.apk" \
    "$APK/Madrassa-EduNoor-v7.1-aligned-unsigned.apk"

echo "Alignment: OK"

echo
echo "[8/8] Signing APK..."

# Keep the debug signing key outside the repository. It is a local
# development credential, never a release secret.
KEYSTORE="$HOME/.android/debug.keystore"

if [ ! -f "$KEYSTORE" ]; then
    echo "Creating debug keystore..."

    keytool \
        -genkeypair \
        -keystore "$KEYSTORE" \
        -storepass android \
        -keypass android \
        -alias androiddebugkey \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

apksigner sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    --out "$APK/Madrassa-EduNoor-v7.1-debug.apk" \
    "$APK/Madrassa-EduNoor-v7.1-aligned-unsigned.apk"

echo
echo "=========================================="
echo " BUILD COMPLETE"
echo "=========================================="

echo
echo "APK:"
ls -lh "$APK/Madrassa-EduNoor-v7.1-debug.apk"

echo
echo "Signature verification:"
apksigner verify \
    --verbose \
    "$APK/Madrassa-EduNoor-v7.1-debug.apk"

echo
echo "SHA-256:"
sha256sum "$APK/Madrassa-EduNoor-v7.1-debug.apk"

echo
echo "=========================================="
echo " V7.1 APK READY FOR SECOND-DEVICE TEST"
echo "=========================================="
