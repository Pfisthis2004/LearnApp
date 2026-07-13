# LearnApp - Ứng dụng Học tập Thông minh tích hợp AI

LearnApp là một ứng dụng Android hiện đại được thiết kế để hỗ trợ người dùng học ngoại ngữ (tiếng Anh) một cách hiệu quả thông qua các bài học, từ vựng và đặc biệt là khả năng tương tác với trí tuệ nhân tạo (AI).
## 📱 Giao diện ứng dụng

<p align="center">
  <img src="[images/learnapp-showcase.png](https://chatgpt.com/backend-api/estuary/content?id=file_000000007b38720b949a22d64b0db0c6&ts=495535&p=fs&cid=1&sig=a42accef3b5f50bdf2df9b6e651c368dfba1ca6f8e1514d19db6942699eb55da&v=0)" alt="LearnApp Showcase" width="100%">
</p>

## 🌟 Tính năng chính

*   **Học tập đa dạng:** Cung cấp các bài học (Lesson), từ vựng (Vocabulary) và bài báo (Articles) phong phú.
*   **Tích hợp AI:** Trò chuyện và học tập cùng AI (Sử dụng Google Gemini) để giải đáp thắc mắc và luyện tập giao tiếp.
*   **Luyện kỹ năng:** Các hoạt động luyện nghe, nói và làm bài tập trắc nghiệm (Quiz).
*   **Quản lý cá nhân:** Hệ thống Streak để duy trì động lực, chỉnh sửa hồ sơ và quản lý cài đặt cá nhân.
*   **Thông báo:** Nhận thông báo nhắc nhở học tập qua Firebase Cloud Messaging.
*   **Đa phương tiện:** Hỗ trợ phát video/âm thanh phục vụ học tập bằng Media3/ExoPlayer.
*   **Xác thực người dùng:** Đăng ký, đăng nhập an toàn với Firebase Auth và Google Sign-In.

## 🛠 Công nghệ sử dụng

*   **Ngôn ngữ:** Kotlin
*   **Kiến trúc:** MVVM (Model-View-ViewModel)
*   **UI/UX:**
    *   Jetpack Navigation Component: Quản lý điều hướng.
    *   View Binding: Kết nối UI và code hiệu quả.
    *   ConstraintLayout, FlexboxLayout: Xây dựng giao diện linh hoạt.
    *   Glide: Tải và hiển thị hình ảnh.
*   **Dữ liệu:**
    *   Room Database: Lưu trữ dữ liệu cục bộ.
    *   Firebase Realtime Database / Firestore: Lưu trữ và đồng bộ dữ liệu đám mây.
    *   Gson: Xử lý dữ liệu JSON.
*   **AI:** Google Generative AI (Gemini SDK).
*   **Khác:**
    *   Firebase Messaging (FCM): Gửi và nhận thông báo.
    *   Firebase Crashlytics: Theo dõi và báo cáo lỗi ứng dụng.
    *   Media3/ExoPlayer: Trình phát đa phương tiện.

## 📁 Cấu trúc thư mục

```text
com.example.learnapp/
├── Model/          # Định dạng dữ liệu (POJO/Data Classes)
├── Repository/     # Quản lý nguồn dữ liệu (Local & Remote)
├── ViewModel/      # Xử lý logic nghiệp vụ và giữ trạng thái UI
├── View/
│   └── ui/
│       ├── activity/    # Các màn hình chính (Activity)
│       ├── fragment/    # Các thành phần giao diện (Fragment)
│       ├── adapter/     # Các trình điều phối dữ liệu cho RecyclerView
│       ├── begin/       # Giao diện chào mừng/hướng dẫn
│       └── bottomsheet/ # Các bảng điều khiển phụ từ dưới lên
└── Utils/          # Các lớp tiện ích (Notification, Helpers...)
```

## 🚀 Cài đặt

1.  **Clone dự án:**
    ```bash
    git clone https://github.com/your-username/LearnApp.git
    ```
2.  **Cấu hình Firebase:**
    *   Tạo project trên [Firebase Console](https://console.firebase.google.com/).
    *   Thêm ứng dụng Android với package name `com.example.learnapp`.
    *   Tải tệp `google-services.json` và đặt vào thư mục `app/`.
3.  **Cấu hình API Key:**
    *   Mở tệp `local.properties` trong thư mục gốc.
    *   Thêm Gemini API Key của bạn:
        ```properties
        GEMINI_API_KEY=YOUR_API_KEY_HERE
        ```
4.  **Build và Chạy:**
    *   Mở dự án bằng Android Studio.
    *   Thực hiện **Gradle Sync**.
    *   Nhấn **Run** để cài đặt ứng dụng lên thiết bị/giả lập.

## 🤝 Đóng góp

Mọi đóng góp nhằm cải thiện ứng dụng đều được trân trọng. Vui lòng tạo Issue hoặc Pull Request nếu bạn có ý tưởng mới hoặc phát hiện lỗi.

---
© 2026 LearnApp Team
