-keep class org.apache.http.** {
    *;
}

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keepattributes InnerClasses
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field
-ignorewarnings

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.merseyside.template.**$$serializer { *; }
-keepclassmembers class com.merseyside.template.** {
    *** Companion;
}
-keepclasseswithmembers class com.merseyside.template.** {
    kotlinx.serialization.KSerializer serializer(...);
}

