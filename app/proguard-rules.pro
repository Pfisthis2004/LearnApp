# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. Hiển thị chi tiết dòng lỗi khi app bị crash trên Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable

# 2. GIỮ LẠI CẤU TRÚC DATA MODELS (Cực kỳ quan trọng)
# Giúp Firebase Firestore ánh xạ (map) dữ liệu đúng vào các biến, tránh lỗi k2.f
-keep class com.example.learnapp.Model.** { *; }

# 3. GIỮ LẠI CÁC LỚP VIEWMODEL
# Tránh lỗi khi hệ thống Android khôi phục vòng đời ứng dụng
-keep class com.example.learnapp.ViewModel.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# 4. GIỮ LẠI REPOSITORY VÀ UTILS
# Đảm bảo các hàm xử lý dữ liệu và công cụ tiện ích không bị xáo trộn sai cách
-keep class com.example.learnapp.Repository.** { *; }
-keep class com.example.learnapp.Utils.** { *; }

# 5. GIỮ LẠI TẦNG VIEW (ACTIVITY/FRAGMENT)
-keep class com.example.learnapp.View.** { *; }

# 6. CẤU HÌNH ĐẶC BIỆT CHO FIREBASE
# Giúp nhận diện các chú thích (annotations) của Firebase khi đã đóng gói
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# 7. GIỮ LẠI GOOGLE GEMINI AI SDK
# Để tính năng AI nói chuyện không bị lỗi khi chạy bản Release
-keep class com.google.ai.client.generativeai.** { *; }