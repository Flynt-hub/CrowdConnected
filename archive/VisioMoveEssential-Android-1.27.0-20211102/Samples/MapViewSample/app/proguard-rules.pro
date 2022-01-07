# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Dev\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:


# VisioMove Essential
# Do not obfuscate Visioglobe related classes
# Note: Internal Visioglobe classes have already been obfuscated when the SDK
# was packaged
-keep class com.visioglobe.** {
    *;
}
