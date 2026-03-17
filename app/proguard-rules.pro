# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*

# Keep WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <fields>;
}
