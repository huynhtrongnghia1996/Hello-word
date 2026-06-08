# Báo cáo thiết kế Framework Automation Test

## Stack

Java 17, Maven, TestNG, Playwright Java, Rest Assured, PostgreSQL, Allure, Log4j2, Git, Jenkins

## 1. Mục tiêu framework

- Xây dựng framework automation test dùng Java 17, Maven, TestNG, Playwright Java, Rest Assured, PostgreSQL, Allure, Log4j2, Git và Jenkins.
- Framework cần hỗ trợ kiểm thử UI, API, database validation, báo cáo trực quan, logging đầy đủ và chạy tự động trong CI/CD.

## 2. Kiến trúc tổng thể

- Framework được chia thành các layer: Test Layer, Business Layer, Core Layer, Reporting Layer và Execution Layer.
- Cách chia này giúp test case ngắn gọn, dễ đọc, giảm trùng lặp và dễ mở rộng khi dự án tăng số lượng test.

## 3. Cấu trúc project đề xuất

- src/main/java/core/config: quản lý cấu hình theo môi trường.
- src/main/java/core/driver: quản lý Playwright, browser, context và page.
- src/main/java/core/api: base API client, request builder, response validator và auth helper.
- src/main/java/core/database: quản lý kết nối PostgreSQL và thực thi query.
- src/main/java/pages: Page Object Model cho UI.
- src/test/java/tests: UI tests, API tests và database tests.
- src/test/resources: TestNG suites, config files, log4j2.xml và allure.properties.

## 4. Thiết kế UI Automation

- Sử dụng Playwright Java theo Page Object Model.
- Mỗi page class chứa locator và action tương ứng với màn hình/chức năng.
- Browser/context/page được quản lý trong core driver để hỗ trợ parallel execution.
- Khi test fail, framework chụp screenshot, lưu trace/video nếu bật cấu hình và attach vào Allure.

## 5. Thiết kế API Automation

- Rest Assured được sử dụng để tạo request và validate response.
- BaseApiClient chuẩn hóa base URI, header, authentication và timeout.
- ResponseValidator hỗ trợ validate status code, JSON body, schema và response time.
- Request/response nên được attach vào Allure để debug nhanh khi CI fail.

## 6. Thiết kế Database Layer

- PostgreSQL JDBC được dùng cho validation dữ liệu và setup/cleanup test data.
- Credential không hardcode trong source code; ưu tiên biến môi trường hoặc Jenkins Credentials.
- DB validation chỉ dùng cho các luồng cần xác nhận dữ liệu backend, tránh lạm dụng gây test chậm và khó bảo trì.

## 7. Reporting và Logging

- Allure cung cấp báo cáo theo Epic, Feature, Story, Severity, step và attachment.
- Log4j2 ghi log console/file, hỗ trợ rolling file và cấu hình log level.
- Artifacts quan trọng gồm screenshots, traces, API request/response, logs và allure-results.

## 8. TestNG Execution Strategy

- Tạo các suite XML: testng-smoke.xml, testng-regression.xml, testng-ui.xml, testng-api.xml và testng-database.xml.
- Sử dụng group smoke/regression/ui/api/database để lọc test linh hoạt.
- Dùng listener cho screenshot, log attachment, retry failed test và custom reporting metadata.

## 9. Jenkins Pipeline

- Pipeline gồm các stage: Checkout, Setup Java 17, Build, Run Tests, Generate/Publish Allure Report, Archive Artifacts và Notification.
- Pipeline parameter gồm environment, suite, browser, headless và threadCount.
- Jenkins nên chạy smoke/API nhanh cho pull request và chạy regression theo lịch hoặc trước release.

## 10. Git Strategy

- Sử dụng branch main/develop/feature/bugfix/release tùy quy trình team.
- Không commit trực tiếp vào main; mọi thay đổi cần Pull Request và review.
- Commit message nên rõ mục đích: feat, test, fix, chore, refactor.

## 11. Roadmap triển khai

- Phase 1: tạo nền tảng Maven, Java 17, config, logging và TestNG base.
- Phase 2: tích hợp Playwright Java và sample UI test.
- Phase 3: tích hợp Rest Assured và sample API test.
- Phase 4: tích hợp PostgreSQL và sample DB validation.
- Phase 5: hoàn thiện Allure, Jenkinsfile, documentation và coding convention.
