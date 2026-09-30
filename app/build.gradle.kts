plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.patel.universalvoicecontrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.patel.universalvoicecontrol"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
