# Release build does not enable minification (isMinifyEnabled = false), so these rules are
# only a safety net. The Jellyfin SDK relies on kotlinx.serialization; keep its generated
# serializers if shrinking is ever turned on.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keep,includedescriptorclasses class org.jellyfin.sdk.model.** { *; }
-keep class kotlinx.serialization.** { *; }
