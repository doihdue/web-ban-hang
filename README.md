# Hướng dẫn chạy project (Demo Spring Boot)

Tệp này hướng dẫn cách cấu hình và chạy project Spring Boot có sẵn trong thư mục này trên máy Windows (PowerShell). Nội dung viết bằng tiếng Việt.

---

## Checklist (những bước tôi sẽ hướng dẫn)
- [x] Yêu cầu môi trường & cài đặt cần thiết
- [x] Cấu hình dữ liệu (DB) trong `src/main/resources/application.properties`
- [x] Chạy ứng dụng bằng Maven wrapper (PowerShell)
- [x] Build file JAR và chạy trực tiếp bằng `java -jar`
- [x] Mẹo debug / xử lý lỗi phổ biến

---

## Yêu cầu môi trường
- Java 17 (pom.xml dự án khai báo `<java.version>17</java.version>`)
- Maven (không bắt buộc nếu dùng Maven Wrapper `mvnw` có sẵn) hoặc sử dụng `mvnw.cmd` trên Windows
- Cơ sở dữ liệu: MySQL (thông thường dự án dùng MySQL theo dependencies). Nếu dùng Oracle, hãy cấu hình tương ứng.

---

## Cấu hình trước khi chạy
Mở file `src/main/resources/application.properties` và sửa các thông số kết nối cơ sở dữ liệu, email, và các tuỳ chọn môi trường theo máy của bạn. Ví dụ mẫu (thay bằng giá trị thực tế của bạn):

spring.datasource.url=jdbc:mysql://localhost:3306/demo_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

Một số tuỳ chọn hữu ích khi phát triển:

spring.thymeleaf.cache=false         # tắt cache template để thay đổi HTML thấy ngay
logging.level.org.springframework=INFO

---

## Chạy project trong PowerShell (Phát triển)
Sử dụng Maven Wrapper (tương thích với dự án):

# Nếu đang ở PowerShell (Windows):
.
```
# Chạy trực tiếp (khuyên dùng trên Windows PowerShell)
.\mvnw.cmd spring-boot:run

# Hoặc nếu bạn có Maven cài đặt trên máy
mvn spring-boot:run
```

Lệnh trên biên dịch và chạy ứng dụng; mặc định server sẽ lắng nghe ở cổng 8080 (hoặc cổng đã cấu hình).

---

## Build file JAR và chạy (sử dụng trong production hoặc test nhanh)

```
# Build (bỏ qua test nếu cần tốc độ):
.\mvnw.cmd clean package -DskipTests

# Chạy file JAR tạo ra (đường dẫn trong target/):
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Ghi chú: Bạn có thể thêm tham số JVM hoặc override cấu hình:
java -Xms256m -Xmx1024m -jar target/demo-0.0.1-SNAPSHOT.jar --server.port=8081
```

---

## Cấu hình thay đổi (một số tình huống thường gặp)
- Port: `--server.port=XXXX` khi chạy jar hoặc thiết lập `server.port` trong `application.properties`.
- Profiles: dùng `--spring.profiles.active=dev` để chạy profile `dev` nếu dự án có cấu hình.

---

## Khởi tạo dữ liệu khi ứng dụng chạy (Roles, Admin mặc định, Categories)
Project có một lớp `DataInitializer` (tại `src/main/java/com/example/demo/config/DataInitializer.java`) chạy lúc khởi động để đảm bảo một số dữ liệu mặc định tồn tại. Hành vi chính:

- Tự tạo 2 role nếu chưa có: `admin` và `user`.
- Tạo một tài khoản admin mặc định nếu chưa có: email `admin@admin.com`, mật khẩu `admin123` (mật khẩu được mã hoá trước khi lưu).
- Tạo một số danh mục mẫu nếu chưa có.

Lưu ý:
- Chuỗi khởi tạo này không chạy lại nếu dữ liệu đã tồn tại (kiểm tra bằng email hoặc số lượng danh mục).
- Nếu bạn muốn thay đổi credentials mặc định, hãy chỉnh trong lớp `DataInitializer` trước khi build/run hoặc đổi mật khẩu trực tiếp trong DB.

---

## Đăng nhập Admin & URL
- Trang đăng nhập ở `templates/login.html`. Trong controller, POST login được xử lý tại endpoint `/LoginUser`.
- Sau khi đăng nhập, nếu user có role `admin` ứng dụng sẽ redirect tới `/admin/dashboard` (được xử lý trong `UserController`).

Default admin credentials (nếu DataInitializer chưa bị thay đổi):
- Email: admin@admin.com
- Mật khẩu: admin123

Để đăng nhập:
1. Mở trình duyệt tới http://localhost:8080/login (hoặc URL app của bạn)
2. Điền email và mật khẩu ở form (hoặc truy cập trang đăng nhập nếu project đặt URL khác). Sau khi POST tới `/LoginUser`, nếu là admin sẽ điều hướng tới trang quản trị.

Nếu bạn không thấy trang `/login`, kiểm tra file `templates/login.html` và controller `UserController` trong `src/main/java/com/example/demo/controller`.

---

## Tạo hoặc đổi tài khoản Admin thủ công
1. Thêm trực tiếp vào DB: trong bảng `role` tạo bản ghi `admin` nếu chưa có; sau đó trong bảng `users` tạo user có trường `role_id` tham chiếu tới role `admin`. Mật khẩu phải là giá trị đã mã hoá (project sử dụng `PasswordEncoder` của Spring Security). Có thể tạm thời đặt `spring.jpa.hibernate.ddl-auto=create` và tạo user bằng mã nguồn hoặc thay đổi DataInitializer.

2. Sửa `DataInitializer` để dùng email/mật khẩu khác, rồi rebuild và chạy lại (lưu ý: nếu user đã tồn tại với email đó, DataInitializer sẽ không ghi đè).

3. Sử dụng `UserService`/controller trong ứng dụng (nếu có chức năng tạo user hoặc đăng ký) để tạo user với role `admin` — bạn có thể gọi endpoint signup bằng cách truyền email là `admin` theo logic trong `UserService` (lưu ý: cẩn thận với cơ chế gán role trong SignUp).

---

## Gợi ý cấu trúc project & tệp quan trọng
- `src/main/resources/templates/` – chứa các file Thymeleaf (HTML)
- `src/main/resources/static/` – chứa CSS, JS, images
- `src/main/java/` – code Java, controller, service, repository
- `src/main/resources/application.properties` – cấu hình ứng dụng
- `pom.xml` – khai báo dependency và plugin


# 👤 Chức năng Khách hàng (User)

## 1. 📄 Trang tĩnh & thông tin (Công khai)

* Trang chủ
* Giới thiệu
* Liên hệ
* Chính sách giao hàng

## 2. 🔍 Duyệt & tìm kiếm sản phẩm (Công khai)

* Xem danh sách sản phẩm
* Tìm kiếm sản phẩm 
* Lọc sản phẩm theo tiêu chí
* Phân trang

## 3. 📦 Chi tiết sản phẩm & tương tác (Công khai)

* Xem chi tiết sản phẩm
* Xem sản phẩm liên quan
* Xem / đọc đánh giá

## 4. ⭐ Tương tác đánh giá

* Thêm / ghi nhận đánh giá sản phẩm *(yêu cầu đăng nhập)*

## 5. 🛒 Giỏ hàng (Yêu cầu đăng nhập)

* Thêm sản phẩm vào giỏ
* Xem giỏ hàng
* Cập nhật số lượng
* Xóa sản phẩm khỏi giỏ

## 6. 💳 Thanh toán & đơn hàng (Yêu cầu đăng nhập)

* Checkout / Đặt hàng
* Hiển thị phương thức thanh toán (QR / Bank)
* Xác nhận thanh toán
* Hủy thanh toán
* Trang xác nhận đơn hàng
* Xem lịch sử và chi tiết đơn hàng

## 7. 🔐 Tài khoản & bảo mật

* Đăng ký
* Đăng nhập / Logout
* Quên mật khẩu (gửi email)
* Reset mật khẩu bằng token

  * Form: công khai
  * Thay đổi mật khẩu: yêu cầu xác thực

## 8. 👤 Hồ sơ & địa chỉ (Yêu cầu đăng nhập)

* Xem / cập nhật thông tin cá nhân
* Đổi mật khẩu
* Quản lý địa chỉ giao hàng:

  * Thêm
  * Xóa
  * Đặt mặc định

# 🛠️ Chức năng Quản trị (Admin)

## 1. 📊 Dashboard & Thống kê

* Thống kê sản phẩm
* Thống kê đơn hàng
* Quản lý tồn kho
* Sản phẩm giảm giá
* Sản phẩm mới

## 2. 📦 Quản lý sản phẩm

* Thêm / Sửa / Xóa sản phẩm
* Danh sách sản phẩm
* Upload & quản lý ảnh sản phẩm
* Xem chi tiết sản phẩm

## 3. 🗂️ Quản lý danh mục

* Thêm / Sửa / Xóa danh mục

## 4. 📑 Quản lý đơn hàng

* Danh sách đơn hàng
* Xem chi tiết đơn hàng
* Cập nhật trạng thái đơn

## 5. 👥 Quản lý khách hàng

* Danh sách khách hàng
* Xem chi tiết khách hàng
* Vô hiệu hóa tài khoản

## 6. 🧩 Quản lý nội dung website

* Quản lý nội dung trang chủ (home sections / feature boxes)
* Cập nhật nội dung tĩnh:

  * Giới thiệu
  * Chính sách giao hàng
* Cấu hình hệ thống (Site settings)




