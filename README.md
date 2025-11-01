# 🛒 Website Thương Mại Điện Tử – **WOMTech**

> 🚀 Dự án xây dựng nền tảng **bán hàng trực tuyến đa vai trò** (User, Vendor, Admin, Shipper), tích hợp **chat realtime**, **thanh toán điện tử**, và **quản lý hệ thống toàn diện**.

---

## 🧭 I. Giới thiệu tổng quan

### **1️⃣ Tên đề tài**
**Website Thương Mại Điện Tử – WOMTech**

### **2️⃣ Mô tả ngắn gọn**
**WOMTech** là hệ thống web thương mại điện tử cho phép người dùng:
- Mua sắm trực tuyến, thêm sản phẩm vào giỏ hàng và thanh toán nhanh chóng.  
- Nhà bán hàng (Vendor) có thể mở shop, đăng bán, quản lý sản phẩm và đơn hàng.  
- Hệ thống quản trị (Admin) có thể kiểm soát toàn bộ người dùng, đơn hàng, voucher, và thống kê doanh thu.  
- Shipper tham gia xử lý giao hàng và cập nhật trạng thái đơn.

### **3️⃣ Mục tiêu dự án**
- Xây dựng nền tảng **bán hàng trực tuyến toàn diện, dễ mở rộng và bảo mật cao**.  
- Tích hợp quy trình mua hàng hoàn chỉnh: *Tạo đơn → Thanh toán → Giao hàng → Đánh giá*.  
- Hỗ trợ **nhiều vai trò** với hệ thống **phân quyền động và JWT**.  
- Hướng tới trải nghiệm người dùng hiện đại: **giao diện responsive, realtime chat, OTP, API Payment**.

---

## 👥 II. Đối tượng sử dụng & phạm vi hệ thống

| **Vai trò** | **Mô tả** |
|--------------|-----------|
| 👤 **User (Khách hàng)** | Đăng ký, mua hàng, thanh toán, theo dõi đơn, đánh giá sản phẩm. |
| 🏬 **Vendor (Nhà bán hàng)** | Quản lý shop, sản phẩm, đơn hàng, khuyến mãi. |
| 🚚 **Shipper (Giao hàng)** | Nhận đơn, cập nhật trạng thái giao, thống kê doanh thu cá nhân. |
| 🛠 **Admin (Quản trị viên)** | Quản lý người dùng, danh mục, sản phẩm, doanh thu, voucher, vận chuyển. |
| 👀 **Guest (Khách vãng lai)** | Xem sản phẩm, danh mục, đăng ký tài khoản mới. |

---

## ⚙️ III. Phân tích chức năng theo từng vai trò

### 👤 **1. User (Khách hàng)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Đăng ký / Đăng nhập | OTP qua email, mã hóa BCrypt, JWT token. |
| Xem sản phẩm | Theo danh mục, thương hiệu, tìm kiếm, lọc giá. |
| Giỏ hàng | Thêm, xóa, cập nhật số lượng, lưu theo user. |
| Thanh toán | COD, MoMo, VNPAY – lưu hóa đơn & trạng thái. |
| Theo dõi đơn hàng | Xem trạng thái đơn: *Đang xử lý → Giao → Hoàn tất*. |
| Đánh giá sản phẩm | Viết nhận xét, gửi ảnh/video minh họa. |
| Chat realtime | Chat với vendor qua WebSocket. |
| Quản lý địa chỉ | Thêm/sửa/xóa, chọn mặc định khi checkout. |

---

### 🏬 **2. Vendor (Nhà bán hàng)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Dashboard cửa hàng | Thống kê doanh thu, top sản phẩm, đơn hàng. |
| Quản lý sản phẩm | CRUD sản phẩm, hình ảnh, giá, tồn kho. |
| Quản lý đơn hàng | Cập nhật trạng thái, giao cho shipper. |
| Quản lý voucher | Tạo mã giảm giá riêng cho shop. |
| Chat với khách | Trả lời chat realtime từ khách hàng. |
| Quản lý nhân viên | (Tùy chọn) thêm shipper nội bộ. |

---

### 🚚 **3. Shipper (Nhân viên giao hàng)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Nhận đơn giao | Danh sách đơn được phân công. |
| Cập nhật trạng thái | *Đã nhận – Đang giao – Đã giao – Hủy*. |
| Thống kê cá nhân | Tổng đơn, doanh thu, đơn hủy. |
| Báo cáo sự cố | Gửi thông tin khi có vấn đề giao hàng. |

---

### 🛠 **4. Admin (Quản trị hệ thống)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Quản lý người dùng | CRUD user, phân quyền, khóa tài khoản. |
| Quản lý vendor | Duyệt shop, kích hoạt/tạm ngưng. |
| Quản lý danh mục | CRUD danh mục cha/con. |
| Quản lý sản phẩm | Theo dõi & can thiệp dữ liệu toàn hệ thống. |
| Quản lý voucher | Tạo và quản lý mã giảm giá toàn cục. |
| Quản lý vận chuyển | Thiết lập phí ship theo khu vực. |
| Thống kê hệ thống | Doanh thu theo tháng, sản phẩm, vendor. |

---

### 👀 **5. Guest (Khách vãng lai)**
| **Chức năng** | **Mô tả chi tiết** |
|----------------|--------------------|
| Xem sản phẩm | Hiển thị danh mục, chi tiết, top bán chạy. |
| Tìm kiếm | Theo tên sản phẩm, danh mục, thương hiệu. |
| Đăng ký / Đăng nhập | OTP email để kích hoạt tài khoản. |

---

## 💡 IV. Tính năng nổi bật & sáng tạo

| **Tính năng** | **Mô tả** | **Mục tiêu** |
|----------------|------------|---------------|
| 💬 Chat realtime | WebSocket giữa User ↔ Vendor | Giao tiếp tức thời, tăng tương tác |
| 🔐 Xác thực bảo mật | JWT + OTP + BCrypt | An toàn và hiện đại |
| 💸 Thanh toán điện tử | VNPAY / MOMO | Mô phỏng thực tế thanh toán |
| 🚚 Giao hàng & Shipper | Giao đơn, cập nhật trạng thái | Mô phỏng hoạt động vận hành |
| 🧾 Voucher động | Theo thời gian, loại sản phẩm | Tăng tính linh hoạt marketing |
| 🗺 Theo dõi giao hàng (Tùy chọn) | Nominatim API | Theo dõi vị trí đơn realtime |

---

## 🧱 Công nghệ sử dụng

| **Thành phần** | **Công nghệ** |
|----------------|---------------|
| Frontend | HTML, CSS, JS, Bootstrap, Thymeleaf |
| Backend | Spring Boot 3.5.x (MVC + REST) |
| Database | MySQL / PostgreSQL |
| Authentication | Spring Security, JWT, BCrypt, OTP |
| Realtime | Spring WebSocket |
| Payment | VNPAY, MOMO API |
| Deploy | Railway / AWS / Render |
| Dev Tools | Maven, Lombok, JPA, Cloudinary API |

---

## 📂 Cấu trúc thư mục chính

```bash
src/
├── main/java/com/womtech/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── config/
│   ├── util/
│   └── security/
└── resources/
    ├── templates/
    ├── static/
    ├── application.yml
    └── email/ 
```

---


## 🏁 Kết luận

**WOMTech** là hệ thống thương mại điện tử hướng đến:
- **Trải nghiệm người dùng mượt mà**  
- **Bảo mật mạnh mẽ và phân quyền chặt chẽ**  
- **Mở rộng dễ dàng cho nhiều cửa hàng & vai trò**  

> 🧾 *“WOMTech – Nền tảng bán hàng thông minh, kết nối mọi vai trò.”* 🚀  

---

📌 **© 2025 WOMTech Team** | *All rights reserved.*
