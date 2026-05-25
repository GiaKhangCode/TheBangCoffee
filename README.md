# TheBangCoffee

<<<<<<< HEAD
TheBangCoffee là ứng dụng quản lý quán cà phê viết bằng **Java Desktop App** sử dụng **Maven** và **Oracle Database**.

> Ghi chú: Repo này không phải backend/frontend web. Code hiện tại có dùng Java Swing/AWT cho giao diện desktop, ví dụ `MainFrame extends JFrame` và main class dùng `SwingUtilities.invokeLater(...)`.
=======
TheBangCoffee là ứng dụng quản lý quán cà phê.
>>>>>>> bf8adb236690642b6fe636f2dbc41ea015880f8c

---

## 1. Công nghệ sử dụng

<<<<<<< HEAD
- Java Desktop App
- Java Swing/AWT
- Maven
=======
>>>>>>> bf8adb236690642b6fe636f2dbc41ea015880f8c
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

CREATE USER TheBangCoffee_Code IDENTIFIED BY "Admin123";

GRANT CONNECT, RESOURCE TO TheBangCoffee_Code;

ALTER USER TheBangCoffee_Code QUOTA UNLIMITED ON USERS;

GRANT CREATE VIEW TO TheBangCoffee_Code;
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

## 7. Cấu hình database an toàn hơn

Hiện tại file kết nối Oracle đang hard-code thông tin database trong:

```txt
src/main/java/ConnectDatabase/ConnectionOracle.java
```

Không nên để username/password database trực tiếp trong code khi push lên GitHub.

Nên sửa sang dạng đọc biến môi trường:

```java
package ConnectDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionOracle {

    public static Connection getOracleConnection() throws ClassNotFoundException, SQLException {
        String hostName = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "1521");
        String sid = System.getenv().getOrDefault("DB_SID", "orcl");
        String userName = System.getenv().getOrDefault("DB_USERNAME", "TheBangClone");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "Admin123");

        Class.forName("oracle.jdbc.driver.OracleDriver");

        String connectionURL = "jdbc:oracle:thin:@" + hostName + ":" + port + ":" + sid;

        return DriverManager.getConnection(connectionURL, userName, password);
    }
}
```

Nếu Oracle của bạn dùng service name thay vì SID, ví dụ `XEPDB1`, đổi URL thành:

```java
String serviceName = System.getenv().getOrDefault("DB_SERVICE", "XEPDB1");
String connectionURL = "jdbc:oracle:thin:@" + hostName + ":" + port + "/" + serviceName;
```

---

## 8. Cấu hình Gmail để gửi OTP/email

Project có chức năng gửi email bằng Gmail SMTP thông qua JavaMail.

File gửi mail:

```txt
src/main/java/Common/EmailUtil.java
```

Không nên hard-code Gmail và Gmail App Password trong file Java.

Nên sửa `EmailUtil.java` sang dạng đọc biến môi trường:

```java
package Common;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtil {

    private static final String FROM_EMAIL = System.getenv("MAIL_USERNAME");
    private static final String APP_PASSWORD = System.getenv("MAIL_PASSWORD");

    public static void sendOTP(String toEmail, String otp, String purpose) {
        if (FROM_EMAIL == null || FROM_EMAIL.isBlank()
                || APP_PASSWORD == null || APP_PASSWORD.isBlank()) {
            throw new IllegalStateException(
                "Missing MAIL_USERNAME or MAIL_PASSWORD environment variable."
            );
        }

        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(
            props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            }
        );

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM_EMAIL));

            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );

            String subject = "xác thực " + purpose;

            message.setSubject("[TheBangCF] Mã OTP " + subject);

            String content = "<h3>Xin chào,</h3>"
                    + "<p>Mã OTP của bạn là:</p>"
                    + "<h2 style='color:blue;'>" + otp + "</h2>"
                    + "<p>Mã này sẽ hết hạn sau <b>5 phút</b>.</p>"
                    + "<p>Không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "<br><p>Trân trọng,<br>TheBangCF Team</p>";

            message.setContent(content, "text/html; charset=UTF-8");

            Transport.send(message);

        } catch (MessagingException e) {
            System.out.println("Gửi email thất bại");
            e.printStackTrace();
        }
    }
}
```

### Tạo Gmail App Password

Không dùng mật khẩu Gmail chính. Hãy dùng **Google App Password**.

Điều kiện:

- Tài khoản Google phải bật xác minh 2 bước
- App Password thường là mã 16 ký tự
- App Password chỉ dùng cho app này

Trang tạo App Password:

```txt
https://myaccount.google.com/apppasswords
```

---

## 9. Set biến môi trường

### Windows PowerShell

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="1521"
$env:DB_SID="orcl"
$env:DB_USERNAME="TheBangClone"
$env:DB_PASSWORD="Admin123"

$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_new_gmail_app_password"
```

Sau đó chạy app trong cùng terminal.

Nếu muốn lưu vĩnh viễn:

```powershell
setx DB_HOST "localhost"
setx DB_PORT "1521"
setx DB_SID "orcl"
setx DB_USERNAME "TheBangClone"
setx DB_PASSWORD "Admin123"

setx MAIL_USERNAME "your_email@gmail.com"
setx MAIL_PASSWORD "your_new_gmail_app_password"
```

Sau khi dùng `setx`, hãy tắt terminal và mở lại terminal mới.

### Windows CMD

```bat
set DB_HOST=localhost
set DB_PORT=1521
set DB_SID=orcl
set DB_USERNAME=TheBangClone
set DB_PASSWORD=Admin123

set MAIL_USERNAME=your_email@gmail.com
set MAIL_PASSWORD=your_new_gmail_app_password
```

### Linux/macOS

```bash
export DB_HOST="localhost"
export DB_PORT="1521"
export DB_SID="orcl"
export DB_USERNAME="TheBangClone"
export DB_PASSWORD="Admin123"

export MAIL_USERNAME="your_email@gmail.com"
export MAIL_PASSWORD="your_new_gmail_app_password"
```

---

## 10. Nếu đã lỡ push Gmail App Password lên GitHub

Nếu Gmail App Password đã từng bị push lên GitHub, hãy xử lý ngay.

### Bước 1: Thu hồi App Password cũ

Vào:

```txt
https://myaccount.google.com/apppasswords
```

Xóa app password cũ đã bị lộ.

Sau khi xóa, app password cũ sẽ không dùng để gửi mail được nữa.

### Bước 2: Tạo App Password mới

Tạo app password mới và chỉ lưu bằng biến môi trường `MAIL_PASSWORD`.

Không ghi app password mới vào:

```txt
EmailUtil.java
README.md
.env
commit message
ảnh chụp màn hình
file log
```

### Bước 3: Sửa code gửi mail

Sửa `EmailUtil.java` để đọc:

```java
System.getenv("MAIL_USERNAME");
System.getenv("MAIL_PASSWORD");
```

thay vì hard-code trực tiếp.

### Bước 4: Commit bản đã xóa secret khỏi code hiện tại

```bash
git add src/main/java/Common/EmailUtil.java
git commit -m "Use environment variables for mail credentials"
git push origin main
```

### Bước 5: Xóa secret khỏi Git history nếu repo public

Nếu secret đã nằm trong commit cũ, chỉ commit bản mới là chưa đủ vì secret vẫn có thể nằm trong lịch sử Git.

Cài `git-filter-repo`:

```bash
pip install git-filter-repo
```

Xóa file chứa secret khỏi toàn bộ lịch sử:

```bash
git filter-repo --path src/main/java/Common/EmailUtil.java --invert-paths --force
```

Sau đó tạo lại `EmailUtil.java` bản an toàn, commit và force push:

```bash
git add src/main/java/Common/EmailUtil.java
git commit -m "Recreate safe email utility"
git push --force origin main
```

Lưu ý:

- `git push --force` sẽ rewrite history trên GitHub.
- Nếu làm nhóm, cần báo các thành viên clone lại repo hoặc reset lại branch.
- Dù đã xóa Git history, vẫn phải thu hồi app password cũ.

---

## 11. Cấu hình PayOS nếu app có dùng thanh toán

Trong `pom.xml` có dependency PayOS Java SDK. Nếu project dùng PayOS, không nên hard-code các key trong code.

Nên dùng biến môi trường:

```txt
PAYOS_CLIENT_ID
PAYOS_API_KEY
PAYOS_CHECKSUM_KEY
```

Ví dụ trên Windows PowerShell:

```powershell
$env:PAYOS_CLIENT_ID="your_client_id"
$env:PAYOS_API_KEY="your_api_key"
$env:PAYOS_CHECKSUM_KEY="your_checksum_key"
```

Ví dụ trong Java:

```java
String clientId = System.getenv("PAYOS_CLIENT_ID");
String apiKey = System.getenv("PAYOS_API_KEY");
String checksumKey = System.getenv("PAYOS_CHECKSUM_KEY");
```

---

## 12. Build project

Tại thư mục gốc project:

```bash
mvn clean compile
```

Chạy test:

```bash
mvn test
```

Build package:

```bash
mvn clean package
```

File build sẽ nằm trong:

```txt
target/
```

Không push thư mục `target/` lên GitHub.

---

## 13. Chạy app

### Cách 1: Chạy bằng IDE

Khuyến nghị chạy bằng NetBeans hoặc IntelliJ IDEA:

1. Mở project
2. Đợi Maven tải dependency
3. Kiểm tra JDK đang dùng là JDK 22
4. Chạy file:

```txt
src/main/java/com/is216/thebangcf/TheBangCF.java
```

Main class:

```txt
com.is216.thebangcf.TheBangCF
```

### Cách 2: Chạy bằng Maven Exec

Từ thư mục gốc project:

```bash
mvn exec:java -Dexec.mainClass="com.is216.thebangcf.TheBangCF"
```

Nếu command trên không chạy do thiếu plugin, thêm plugin sau vào `pom.xml` trong thẻ `<plugins>`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <mainClass>com.is216.thebangcf.TheBangCF</mainClass>
    </configuration>
</plugin>
```

Sau đó chạy:

```bash
mvn exec:java
```

### Cách 3: Chạy bằng command line sau khi build

Copy dependency ra thư mục `target/dependency`:

```bash
mvn clean package dependency:copy-dependencies
```

Windows PowerShell/CMD:

```bat
java -cp "target/classes;target/dependency/*" com.is216.thebangcf.TheBangCF
```

Linux/macOS:

```bash
java -cp "target/classes:target/dependency/*" com.is216.thebangcf.TheBangCF
```

---

## 14. Một số lỗi thường gặp

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

## 15. `.gitignore` khuyến nghị

Tạo file `.gitignore` ở thư mục gốc project để không push file build, log, IDE và secret.

Các file/thư mục nên ignore:

```txt
target/
*.class
hs_err_pid*.log
replay_pid*.log
.env
.idea/
.vscode/
nbproject/private/
```

Nếu các file này đã lỡ bị Git track, `.gitignore` chưa đủ. Cần dùng `git rm --cached`.

---

## 16. Dọn file rác đã lỡ push lên GitHub

Repo hiện có một số file/thư mục không nên nằm trên GitHub:

```txt
target/
GetFunctions.class
hs_err_pid11952.log
hs_err_pid13924.log
hs_err_pid6004.log
replay_pid13924.log
```

Trong đó:

- `target/`: thư mục build của Maven
- `*.class`: file bytecode build ra từ Java
- `hs_err_pid*.log`: JVM crash log
- `replay_pid*.log`: JVM replay/debug log

### Cách A: Xóa khỏi branch hiện tại, giữ file local trên máy

Cách này đủ cho đa số trường hợp. Sau khi push, các file rác sẽ không còn xuất hiện ở branch `main` hiện tại.

Chạy tại thư mục gốc repo:

```bash
git checkout main
git pull origin main

git rm -r --cached --ignore-unmatch target
git rm --cached --ignore-unmatch GetFunctions.class
git rm --cached --ignore-unmatch 'hs_err_pid*.log'
git rm --cached --ignore-unmatch 'replay_pid*.log'

git add .gitignore README.md
git status
git commit -m "Update README and remove generated files"
git push origin main
```

Giải thích:

- `git rm --cached`: xóa file khỏi Git index nhưng không xóa file thật trên máy.
- `--ignore-unmatch`: không báo lỗi nếu file không tồn tại.
- `.gitignore`: ngăn các file rác bị add lại trong tương lai.

Nếu muốn xóa luôn file rác khỏi máy local:

```bash
rm -rf target
rm -f GetFunctions.class
rm -f hs_err_pid*.log
rm -f replay_pid*.log
```

Windows PowerShell:

```powershell
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
Remove-Item -Force GetFunctions.class -ErrorAction SilentlyContinue
Remove-Item -Force hs_err_pid*.log -ErrorAction SilentlyContinue
Remove-Item -Force replay_pid*.log -ErrorAction SilentlyContinue
```

### Cách B: Xóa thư mục/file phụ nếu thật sự không cần

Nếu `view-tham-khao/` chỉ là folder tham khảo, không cần nộp cùng source chính, có thể xóa khỏi Git:

```bash
git rm -r --cached --ignore-unmatch view-tham-khao
```

Sau đó thêm vào `.gitignore`:

```gitignore
view-tham-khao/
```

Rồi commit:

```bash
git add .gitignore
git commit -m "Remove reference UI folder from repository"
git push origin main
```

Nếu `GetFunctions.java` chỉ là file test query tạm thời và không thuộc app chính, có thể xóa khỏi Git:

```bash
git rm --cached --ignore-unmatch GetFunctions.java
echo GetFunctions.java >> .gitignore
git add .gitignore
git commit -m "Remove temporary database helper"
git push origin main
```

Nếu vẫn cần dùng `GetFunctions.java` để debug, hãy giữ lại.

### Cách C: Xóa file rác khỏi toàn bộ Git history

Cách này chỉ cần khi:

- File rác quá lớn làm repo nặng
- Secret/password/API key đã lộ trong commit cũ
- Muốn GitHub không còn lưu file đó trong lịch sử commit

Cài `git-filter-repo`:

```bash
pip install git-filter-repo
```

Chạy tại thư mục gốc repo:

```bash
git checkout main
git pull origin main

git filter-repo \
  --path target/ \
  --path GetFunctions.class \
  --path-glob 'hs_err_pid*.log' \
  --path-glob 'replay_pid*.log' \
  --invert-paths \
  --force
```

Sau đó push force:

```bash
git remote add origin https://github.com/GiaKhangCode/TheBangCoffee.git
git push --force origin main
```

Nếu repo có tag:

```bash
git push --force --tags
```

Lưu ý rất quan trọng:

- `git filter-repo` sẽ rewrite lịch sử Git.
- Sau khi force push, các thành viên khác nên clone lại repo.
- Nếu chỉ muốn xóa file khỏi branch hiện tại, dùng Cách A là đủ.
- Nếu đã lộ Gmail App Password hoặc API key, bắt buộc phải thu hồi/rotate secret, không chỉ xóa Git history.

---

## 17. Lệnh kiểm tra sau khi dọn

Kiểm tra file rác còn bị Git track không:

```bash
git ls-files target
git ls-files '*.class'
git ls-files 'hs_err_pid*.log'
git ls-files 'replay_pid*.log'
```

Nếu các lệnh trên không in gì ra, nghĩa là Git không còn track các file đó.

Kiểm tra `.gitignore` có hoạt động không:

```bash
git status --ignored
```

Build lại để chắc chắn app vẫn chạy:

```bash
mvn clean compile
```

Chạy app:

```bash
mvn exec:java -Dexec.mainClass="com.is216.thebangcf.TheBangCF"
```

---

## 18. Quy trình chạy nhanh

```bash
git clone https://github.com/GiaKhangCode/TheBangCoffee.git
cd TheBangCoffee

# 1. Tạo Oracle user TheBangClone
# 2. Import COFFEE.sql
# 3. Set biến môi trường DB và Gmail nếu đã sửa code đọc env
# 4. Build app

mvn clean compile

# 5. Chạy app
mvn exec:java -Dexec.mainClass="com.is216.thebangcf.TheBangCF"
```

---

## 19. Lưu ý bảo mật

Không nên commit các thông tin sau lên GitHub:

- Gmail App Password
- Password database thật
- PayOS API key
- PayOS checksum key
- File `.env`
- File log có thể chứa stack trace hoặc thông tin máy
- Thư mục `target/`
- File `.class`

Nếu secret đã lỡ bị push lên GitHub, hãy thu hồi/rotate secret ngay, sau đó xóa khỏi code hiện tại và cân nhắc xóa khỏi Git history.
