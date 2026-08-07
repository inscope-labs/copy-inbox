# Agent Task Report: Wire Version Into Gradle Builds

- **Timestamp (UTC)**: 2026-08-06T23:43:10Z
- **Short Slug**: wire-version-into-gradle-builds

## What Was Asked
- Wire `versionCode`/`versionName` into actual Gradle builds for both debug and release using local files only.
- In `/app/build.gradle.kts`, replace literal `versionCode` and `versionName` with `project.findProperty(...)` checks with fallback values `3` and `"1.0"`.
- In `/.github/workflows/build-apk-debug.yml`, add `Read version info` step reading `versionCode`, `versionName`, `debugCode` from `version.properties` and pass `-PversionCode` and `-PversionName` to `assembleDebug`.
- In `/.github/workflows/build-apk-release.yml`, read `versionCode` from `version.properties`, pass `-PversionCode` and `-PversionName` to `assembleRelease`, and add a `Persist release-code.txt` step persisting the incremented `releasePatch` back to `release-code.txt` on successful build.
- In `/AGENTS.md`, add clarifying notes under Section 2 and Section 2a regarding shared `versionCode` and automated `releasePatch` persistence by CI.

## Version Assessment
- **Assessed Probability Score**: 30 / 100 (CI configuration and build scripting changes; no new debug build trigger required).
- **Version Action**: No change to `version.properties` (`versionCode=3`, `debugCode=0003`).

## Exact Before/After Changes

### 1. `/app/build.gradle.kts`
**Before:**
```kotlin
  defaultConfig {
    applicationId = "com.inscopelabs.abx.clipinbox"
    minSdk = 24
    targetSdk = 36
    versionCode = 3
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
```

**After:**
```kotlin
  defaultConfig {
    applicationId = "com.inscopelabs.abx.clipinbox"
    minSdk = 24
    targetSdk = 36
    versionCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 3
    versionName = project.findProperty("versionName")?.toString() ?: "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
```

### 2. `/.github/workflows/build-apk-debug.yml` Diff
```diff
@@ -48,8 +48,29 @@
             echo "Using existing debug.keystore"
           fi

+      - name: Read version info
+        id: read_version
+        run: |
+          PROP_FILE="version.properties"
+          if [ -f "$PROP_FILE" ]; then
+            VERSION_CODE=$(grep '^versionCode=' "$PROP_FILE" | cut -d'=' -f2 | tr -d '\r')
+            VERSION_NAME=$(grep '^versionName=' "$PROP_FILE" | cut -d'=' -f2 | tr -d '\r')
+            DEBUG_CODE=$(grep '^debugCode=' "$PROP_FILE" | cut -d'=' -f2 | tr -d '\r')
+          fi
+
+          VERSION_CODE=${VERSION_CODE:-1}
+          VERSION_NAME=${VERSION_NAME:-1.0}
+          DEBUG_CODE=${DEBUG_CODE:-0001}
+
+          echo "version_code=$VERSION_CODE" >> $GITHUB_OUTPUT
+          echo "version_name=$VERSION_NAME" >> $GITHUB_OUTPUT
+          echo "debug_code=$DEBUG_CODE" >> $GITHUB_OUTPUT
+
       - name: Assemble Debug APK
-        run: gradle --no-daemon assembleDebug
+        run: |
+          gradle --no-daemon assembleDebug \
+            -PversionCode=${{ steps.read_version.outputs.version_code }} \
+            -PversionName="${{ steps.read_version.outputs.version_name }}.${{ steps.read_version.outputs.debug_code }}"

       - name: Run Debug Unit Tests
         run: gradle --no-daemon testDebugUnitTest
```

### 3. `/.github/workflows/build-apk-release.yml` Diff
```diff
@@ -44,6 +44,12 @@
           MINOR=${MINOR:-1}
           PATCH=${PATCH:-0}

+          VERSION_CODE=1
+          if [ -f "version.properties" ]; then
+            VC=$(grep '^versionCode=' "version.properties" | cut -d'=' -f2 | tr -d '\r')
+            VERSION_CODE=${VC:-1}
+          fi
+
           echo "Read version: $MAJOR.$MINOR.$PATCH"

           # Increment patch variable in memory for workflow
@@ -50,7 +56,11 @@

           FULL_VERSION="$MAJOR.$MINOR.$NEW_PATCH"
           echo "Workflow release version: $FULL_VERSION"
+          echo "version_code=$VERSION_CODE" >> $GITHUB_OUTPUT
           echo "version_name=$FULL_VERSION" >> $GITHUB_OUTPUT
+          echo "release_major=$MAJOR" >> $GITHUB_OUTPUT
+          echo "release_minor=$MINOR" >> $GITHUB_OUTPUT
+          echo "release_patch=$NEW_PATCH" >> $GITHUB_OUTPUT

       - name: Ensure release keystore exists
         run: |
@@ -73,7 +83,10 @@
           KEYSTORE_PATH: ${{ github.workspace }}/my-upload-key.jks
           STORE_PASSWORD: android
           KEY_PASSWORD: android
-        run: gradle --no-daemon assembleRelease
+        run: |
+          gradle --no-daemon assembleRelease \
+            -PversionCode=${{ steps.versioning.outputs.version_code }} \
+            -PversionName="${{ steps.versioning.outputs.version_name }}"

       - name: Upload Release APK
         if: success()
@@ -81,4 +94,25 @@
           name: app-release
           path: app/build/outputs/apk/release/*.apk
           if-no-files-found: error
+
+      - name: Persist release-code.txt
+        if: success()
+        run: |
+          MAJOR="${{ steps.versioning.outputs.release_major }}"
+          MINOR="${{ steps.versioning.outputs.release_minor }}"
+          NEW_PATCH="${{ steps.versioning.outputs.release_patch }}"
+          FULL_VERSION="${{ steps.versioning.outputs.version_name }}"

+          cat << EOF > release-code.txt
+releaseMajor=$MAJOR
+releaseMinor=$MINOR
+releasePatch=$NEW_PATCH
+EOF

+          git config user.name "github-actions[bot]"
+          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
+          git add release-code.txt
+          git commit -m "ci: record release version $FULL_VERSION"
+          git pull --rebase origin main
+          git push
```

## Commands Executed & Results
- `compile_applet`: Build succeeded cleanly (`assembleDebug`).
- `git status`: Resulted in fatal index error (`fatal: unknown index entry format 0xefbf0000`).

## Assumptions Made
- Verification used local `assembleDebug` compilation (`compile_applet`) without running `assembleRelease` locally per task constraints.

## Errors / Partial Failures / Unverified Items
- `git status` failed with container index entry format error.

## Logging Gap Flags
- N/A (no Kotlin source code files touched).
