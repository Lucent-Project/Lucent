-dontwarn **
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class org.jetbrains.skiko.** { *; }
-keep class androidx.compose.ui.awt.** { *; }
-keep class com.sun.jna.** { *; }
-keep class org.sqlite.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclasseswithmembernames class * {
    native <methods>;
}
