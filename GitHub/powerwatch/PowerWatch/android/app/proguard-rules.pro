# Add project-specific ProGuard rules here.
# Keep serialization models intact.
-keepattributes *Annotation*
-keepclassmembers class com.powerwatch.app.data.remote.** { *; }
-keepclassmembers class com.powerwatch.app.domain.model.** { *; }
