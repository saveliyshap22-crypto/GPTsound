# Keep serialized SoundCloud DTO names stable when minification is enabled later.
-keepattributes *Annotation*
-keep class app.gptsound.android.data.** { *; }
