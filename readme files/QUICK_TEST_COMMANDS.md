# Quick Test Commands for Lost and Found App

## 🚀 Build and Install Commands

```bash
# Clean and build the project
./gradlew clean
./gradlew app:assembleDebug

# Install on connected device/emulator
./gradlew app:installDebug

# Build and install in one command
./gradlew app:installDebug

# Uninstall if needed
adb uninstall com.example.loginandregistration
```

## 📱 Device/Emulator Commands

```bash
# List connected devices
adb devices

# Start an emulator (if you have one configured)
emulator -avd [YOUR_AVD_NAME]

# Launch the app
adb shell am start -n com.example.loginandregistration/.MainActivity
```

## 🔍 Real-time Debugging Commands

```bash
# Monitor app logs in real-time
adb logcat | grep "com.example.loginandregistration"

# Monitor Firebase-specific logs
adb logcat | grep -E "(Firebase|Firestore|FirebaseAuth)"

# Monitor crashes and errors
adb logcat | grep -E "(FATAL|ERROR|AndroidRuntime)"

# Clear logcat buffer (start fresh)
adb logcat -c

# Save logs to file for analysis
adb logcat > app_logs.txt
```

## 🔥 Firebase Testing Commands

### Test Firebase Connection (via ADB)
```bash
# Check if Firebase is initialized (look for Firebase logs)
adb logcat | grep "Firebase"

# Monitor Firestore operations
adb logcat | grep "Firestore"

# Monitor Authentication
adb logcat | grep "FirebaseAuth"
```

## 📊 Performance Monitoring

```bash
# Monitor memory usage
adb shell dumpsys meminfo com.example.loginandregistration

# Monitor CPU usage
adb shell top | grep com.example.loginandregistration

# Monitor network activity
adb shell netstat | grep com.example.loginandregistration
```

## 🧪 Testing Scenarios

### Scenario 1: Fresh Install Test
```bash
# Uninstall completely
adb uninstall com.example.loginandregistration

# Reinstall
./gradlew app:installDebug

# Launch and monitor logs
adb logcat -c && adb logcat | grep "com.example.loginandregistration"
```

### Scenario 2: Network Connectivity Test
```bash
# Disable WiFi (test offline behavior)
adb shell svc wifi disable

# Re-enable WiFi
adb shell svc wifi enable

# Check network state
adb shell dumpsys connectivity
```

### Scenario 3: App State Testing
```bash
# Force stop the app
adb shell am force-stop com.example.loginandregistration

# Restart the app
adb shell am start -n com.example.loginandregistration/.MainActivity

# Put app in background (simulate home button)
adb shell input keyevent KEYCODE_HOME

# Bring app back to foreground
adb shell am start -n com.example.loginandregistration/.MainActivity
```

## 🔧 Debugging Utilities

### Add Debug Code Temporarily
Add this to your MainActivity's onCreate() method for testing:

```kotlin
// Temporary debug code - remove before production
if (BuildConfig.DEBUG) {
    // Test Firebase connection
    DebugHelper.runAllTests()
    
    // Log current user state
    FirebaseAuth.getInstance().addAuthStateListener { auth ->
        Log.d("MAIN_DEBUG", "Auth state changed: ${auth.currentUser?.email ?: "No user"}")
    }
}
```

### Monitor Specific Events
```bash
# Monitor login attempts
adb logcat | grep -i "login\|auth"

# Monitor item creation
adb logcat | grep -i "item\|report"

# Monitor navigation
adb logcat | grep -i "fragment\|activity"
```

## 📋 Quick Verification Checklist

Run these commands to verify your setup:

```bash
# 1. Check if app builds
./gradlew app:assembleDebug && echo "✅ Build successful" || echo "❌ Build failed"

# 2. Check if Firebase config is valid
grep -q "lost-and-found-954f6" app/google-services.json && echo "✅ Firebase config found" || echo "❌ Firebase config missing"

# 3. Check if app installs
./gradlew app:installDebug && echo "✅ Install successful" || echo "❌ Install failed"

# 4. Launch app and check for crashes
adb shell am start -n com.example.loginandregistration/.MainActivity
sleep 5
adb logcat -d | grep "FATAL" && echo "❌ App crashed" || echo "✅ App running"
```

## 🎯 Focused Testing Commands

### Test Authentication Flow
```bash
# Clear app data (reset to fresh state)
adb shell pm clear com.example.loginandregistration

# Launch app
adb shell am start -n com.example.loginandregistration/.MainActivity

# Monitor authentication logs
adb logcat | grep -E "(FirebaseAuth|LOGIN|REGISTER)"
```

### Test Item Management
```bash
# Monitor Firestore operations
adb logcat | grep -E "(Firestore|ITEM|REPORT)"

# Check for data persistence
adb shell am force-stop com.example.loginandregistration
adb shell am start -n com.example.loginandregistration/.MainActivity
```

### Test Admin Features
```bash
# Monitor admin-specific logs
adb logcat | grep -E "(ADMIN|Dashboard|admin@gmail.com)"
```

## 🚨 Emergency Commands

### If App Won't Start
```bash
# Clear app data and cache
adb shell pm clear com.example.loginandregistration

# Reinstall completely
adb uninstall com.example.loginandregistration
./gradlew app:installDebug
```

### If Firebase Issues
```bash
# Check network connectivity
ping google.com

# Verify Firebase project status
# (Check Firebase Console manually)

# Clear app data to reset Firebase state
adb shell pm clear com.example.loginandregistration
```

## 📝 Log Analysis Tips

### Important Log Tags to Watch
- `FirebaseAuth`: Authentication issues
- `Firestore`: Database operations
- `MainActivity`: Main app flow
- `AdminDashboard`: Admin functionality
- `Fragment`: Navigation issues

### Common Error Patterns
```bash
# Permission denied errors
adb logcat | grep -i "permission denied"

# Network errors
adb logcat | grep -i "network\|connection"

# Firebase errors
adb logcat | grep -i "firebase.*error"
```

Remember to remove all debug code and test data before releasing to production!