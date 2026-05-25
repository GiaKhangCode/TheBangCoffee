# TheBangCoffee

TheBangCoffee là ứng dụng quản lý quán cà phê.

---

## 1. Công nghệ sử dụng

- Java Desktop App
- Java Swing
- Maven
- Oracle Database
- Oracle JDBC Driver `ojdbc11`
- JavaMail / Gmail SMTP
- JCalendar
- JFreeChart
- JasperReports
- iText PDF
- ZXing QR Code
- PayOS Java SDK
- JUnit 5

---

## 2. Cấu trúc thư mục

```txt
TheBangCoffee/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Common/
│   │   │   ├── ConnectDatabase/
│   │   │   ├── Controller/
│   │   │   ├── DatabaseAccessObject/
│   │   │   ├── Model/
│   │   │   ├── Service/
│   │   │   ├── View/
│   │   │   └── com/is216/thebangcf/
│   │   │       └── TheBangCF.java
│   │   │
│   │   └── resources/
│   │       ├── images/
│   │       └── reports/
│   │
│   └── test/
│       └── java/
│
├── view-tham-khao/
├── COFFEE.sql
├── query.sql
├── GetFunctions.java
├── pom.xml
├── .gitignore
└── README.md
```

Giải thích nhanh:

- `src/main/java/View/`: giao diện desktop
- `src/main/java/Controller/`: xử lý điều hướng và nghiệp vụ giữa View/DAO/Service
- `src/main/java/Model/`: model/entity
- `src/main/java/DatabaseAccessObject/`: thao tác dữ liệu
- `src/main/java/Service/`: xử lý nghiệp vụ
- `src/main/java/ConnectDatabase/`: kết nối Oracle Database
- `src/main/java/Common/`: tiện ích dùng chung như validate, hash, gửi email
- `src/main/resources/images/`: hình ảnh của app
- `src/main/resources/reports/`: template report
- `COFFEE.sql`: script database chính
- `query.sql`: query hỗ trợ
- `GetFunctions.java`: file tiện ích/test query, không phải luồng chạy chính của app

---

## 3. Yêu cầu cài đặt

Cần cài trước:

- JDK 22 hoặc phiên bản tương thích với `pom.xml`
- Maven
- Oracle Database
- SQL Developer hoặc công cụ chạy SQL tương đương
- Git
- NetBeans hoặc IntelliJ IDEA nếu chạy bằng IDE

Kiểm tra phiên bản:

```bash
java -version
mvn -version
git --version
```

Trong `pom.xml`, project đang cấu hình Java release `22`, vì vậy khuyến nghị dùng JDK 22.

Nếu máy chỉ có JDK 17 hoặc 21, có thể đổi trong `pom.xml`:

```xml
<maven.compiler.release>17</maven.compiler.release>
```

hoặc:

```xml
<maven.compiler.release>21</maven.compiler.release>
```

---

## 4. Clone project

```bash
git clone https://github.com/GiaKhangCode/TheBangCoffee.git
cd TheBangCoffee
```

---

## 5. Tạo Oracle user/schema

Code kết nối database hiện tại đang dùng thông tin mặc định:

```txt
Host    : localhost
Port    : 1521
SID     : orcl
Username: TheBangClone
Password: Admin123
```

Đăng nhập Oracle bằng tài khoản có quyền DBA, ví dụ `SYS AS SYSDBA`, sau đó chạy:

```sql
alter session set "_ORACLE_SCRIPT"=true;

CREATE USER TheBangClone IDENTIFIED BY "Admin123";

GRANT CONNECT, RESOURCE TO TheBangClone;

ALTER USER TheBangClone QUOTA UNLIMITED ON USERS;

GRANT CREATE VIEW TO TheBangClone;
```

Sau khi tạo user, đăng nhập lại bằng:

```txt
Username: TheBangClone
Password: Admin123
```

---

## 6. Import database

Chạy file SQL chính:

```txt
COFFEE.sql
```

Cách chạy bằng Oracle SQL Developer:

1. Mở Oracle SQL Developer
2. Tạo connection bằng user `TheBangClone`
3. Mở file `COFFEE.sql`
4. Chạy toàn bộ script
5. Kiểm tra bảng, dữ liệu mẫu, sequence, procedure/function nếu có

Sau đó có thể dùng `query.sql` để kiểm tra dữ liệu hoặc chạy query hỗ trợ.

---

## 7. Cấu hình Gmail để gửi OTP/email

Project có chức năng gửi email bằng Gmail SMTP thông qua JavaMail.

File gửi mail:

```txt
src/main/java/Common/EmailUtil.java
```

```java
private static final String FROM_EMAIL = System.getenv("MAIL_USERNAME");
private static final String APP_PASSWORD = System.getenv("MAIL_PASSWORD");
```

### Tạo Gmail App Password

Sử dụng **Google App Password**.

Điều kiện:

- Tài khoản Google phải bật xác minh 2 bước
- App Password thường là mã 16 ký tự
- App Password chỉ dùng cho app này

Trang tạo App Password:

```txt
https://myaccount.google.com/apppasswords
```

---


## 8. Cấu hình PayOS nếu app có dùng thanh toán

Java:

```java
String clientId = System.getenv("PAYOS_CLIENT_ID");
String apiKey = System.getenv("PAYOS_API_KEY");
String checksumKey = System.getenv("PAYOS_CHECKSUM_KEY");
```

---

## 9. Một số lỗi thường gặp

### Lỗi không kết nối được Oracle

Kiểm tra:

- Oracle Database đã chạy chưa
- Listener đã chạy chưa
- SID có đúng là `orcl` không
- User `TheBangClone` đã được tạo chưa
- Đã import `COFFEE.sql` chưa
- Password có đúng không

Kiểm tra listener:

```bash
lsnrctl status
```

Nếu Oracle dùng service name như `XEPDB1`, cần đổi connection URL từ dạng SID:

```txt
jdbc:oracle:thin:@localhost:1521:orcl
```

sang dạng service name:

```txt
jdbc:oracle:thin:@localhost:1521/XEPDB1
```

### Lỗi `Unsupported class file major version`

Nguyên nhân thường là JDK không đúng phiên bản.

Project đang cấu hình Java release 22, nên hãy kiểm tra:

```bash
java -version
mvn -version
```

Cách xử lý:

- Cài JDK 22
- Hoặc đổi `maven.compiler.release` trong `pom.xml` về phiên bản JDK đang dùng

### Lỗi gửi email thất bại

Kiểm tra:

- Đã bật xác minh 2 bước cho Google Account chưa
- Đã tạo Gmail App Password chưa
- Đã set `MAIL_USERNAME` chưa
- Đã set `MAIL_PASSWORD` chưa
- Không dùng mật khẩu Gmail chính
- SMTP host là `smtp.gmail.com`
- SMTP port là `587`
- `starttls.enable` là `true`

Kiểm tra biến môi trường:

Windows PowerShell:

```powershell
echo $env:MAIL_USERNAME
```

CMD:

```bat
echo %MAIL_USERNAME%
```

Linux/macOS:

```bash
echo $MAIL_USERNAME
```

Không nên in `MAIL_PASSWORD` nếu đang quay màn hình hoặc chia sẻ máy.

### Lỗi thiếu dependency Maven

Chạy lại:

```bash
mvn clean install
```

Hoặc trong IDE:

- Reload Maven Project
- Clean and Build
- Xóa cache Maven nếu dependency lỗi nặng

---
