# Hướng dẫn tích hợp AI (Cursor) để viết test case theo chuẩn framework

Mục tiêu: khi bạn yêu cầu AI "viết test case", AI sẽ **tự đọc role + rule của framework** và sinh code đúng chuẩn POM, marker, logging, không hardcode bí mật. Tài liệu này giải thích cơ chế và cách dùng.

---

## 1. Cách AI "đọc role và rule" — cơ chế Cursor Rules

Cursor nạp ngữ cảnh cho AI từ hai nguồn đã được thêm vào repo:

### a) `.cursor/rules/*.mdc` (Project Rules)

Mỗi file có phần frontmatter quyết định **khi nào** rule được nạp vào ngữ cảnh:

| File | Loại | Khi nào được nạp |
|---|---|---|
| `framework-role.mdc` | **Always** (`alwaysApply: true`) | Nạp cho **mọi** request → đây là "role" luôn bật |
| `writing-test-cases.mdc` | **Auto Attached** (`globs: tests/**/*.py`) | Tự nạp khi đang làm việc với file trong `tests/` |
| `page-objects.mdc` | **Auto Attached** (`globs: src/automation/pages/**/*.py`) | Tự nạp khi đụng tới page object |

Cơ chế:
- **Always**: luôn nằm trong system prompt → AI luôn biết vai trò + nguyên tắc bất biến.
- **Auto Attached**: khi file bạn mở/đề cập khớp `globs`, rule tương ứng được tự động đính kèm.
- **Manual**: bạn có thể chủ động gọi bất kỳ rule nào bằng `@` trong khung chat, ví dụ `@writing-test-cases`.

### b) `AGENTS.md`

Cursor (và các cloud agent) tự đọc `AGENTS.md` ở gốc repo để lấy hướng dẫn vận hành/môi trường (dùng `--browser chrome`, headless, biến `ACCOUNT_PASSWORD`/`DB_URL`, CI gate...).

> Tài liệu kỹ thuật chi tiết của framework nằm ở `docs/framework-documentation.md`; rule đã trỏ AI tới đây khi cần.

---

## 2. Setup (đã làm sẵn — chỉ cần kéo code về)

Cấu trúc đã được thêm:

```text
.cursor/rules/
├── framework-role.mdc        # ROLE + nguyên tắc bất biến (Always)
├── writing-test-cases.mdc    # Quy ước viết test (Auto Attached: tests/**)
└── page-objects.mdc          # Quy ước page object (Auto Attached: src/automation/pages/**)
AGENTS.md                     # Hướng dẫn môi trường/cloud
docs/framework-documentation.md
```

Không cần cài thêm gì cho phần rule — Cursor tự nhận `.cursor/rules` và `AGENTS.md`. Để kiểm tra: mở **Cursor Settings → Rules**, bạn sẽ thấy 3 rule trên; rule `framework-role` ở trạng thái Always.

(Tuỳ chọn) Tự sinh thêm rule từ codebase: chạy lệnh `/Generate Cursor Rules` trong chat của Cursor.

---

## 3. Cách yêu cầu AI viết test case

Vì `framework-role` luôn bật và `writing-test-cases` tự đính kèm khi ở `tests/`, bạn chỉ cần mô tả nghiệp vụ. Ví dụ prompt tốt:

```text
Viết smoke test mở trang chủ eFMS và kiểm tra URL. Tuân theo @writing-test-cases.
```

```text
Thêm test đăng nhập eTMS dùng fixture account_password (tự skip nếu thiếu mật khẩu),
assert ô password biến mất sau khi login. Theo đúng rule framework.
```

```text
Tôi cần test thao tác tạo đơn trên eFMS. Trước hết thêm method vào page object
theo @page-objects, đăng ký vào PageManager, rồi viết test với marker phù hợp.
```

AI sẽ: đọc role → đọc rule test/page-object → (nếu cần) thêm method page object → viết test dùng `pages`/`account_password` → đề xuất lint/pyright/pytest để xác minh.

---

## 4. Workflow tối ưu (khuyến nghị)

1. Mở file trong `tests/` (để auto-attach rule) hoặc gắn `@writing-test-cases` thủ công.
2. Mô tả luồng nghiệp vụ + hệ thống (eFMS/eTMS) + loại test (smoke/login...).
3. Để AI thêm page method nếu thao tác chưa tồn tại (tránh selector trực tiếp trong test).
4. Yêu cầu AI chạy kiểm chứng:
   ```bash
   uv run ruff check .
   uv run pyright
   uv run pytest -m <marker> --browser chrome --browser-headless true
   ```
5. Review: đảm bảo có marker hợp lệ, không hardcode bí mật, dùng `@log_method`, trả `self` ở method thao tác.

---

## 5. Mở rộng / tuỳ biến

- **Sửa hành vi AI**: chỉnh trực tiếp các file `.cursor/rules/*.mdc` (đổi `globs`, `alwaysApply`, hoặc nội dung quy ước). Commit để cả team/cloud agent dùng chung.
- **Thêm rule mới** cho API/DB: tạo `.cursor/rules/api-tests.mdc` / `db-tests.mdc` với `globs: tests/api/**` / `tests/db/**`.
- **Cloud agent**: thông tin trong `AGENTS.md` sẽ được agent đọc tự động; thêm lưu ý vận hành mới vào mục `## Cursor Cloud specific instructions`.

> Lưu ý: rule chỉ là "ngữ cảnh hướng dẫn", không thay thế review. Luôn chạy lint/type-check/pytest để xác nhận test do AI sinh ra chạy đúng.
