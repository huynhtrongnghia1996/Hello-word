# Tài liệu Framework Automation eFMS/eTMS (Python)

Tài liệu này mô tả chi tiết toàn bộ framework: kiến trúc, từng package, **từng page**, **từng method** và **phần báo cáo (report)**. Mục tiêu để người mới có thể đọc và hiểu/đóng góp ngay.

---

## 1. Tổng quan

Đây là framework kiểm thử tự động (test automation) cho hai hệ thống web:

- **eFMS** – `https://uat-efms.logtechub.com/en/#/home`
- **eTMS** – `https://staging-itllog-etms.logtechub.com/en/#/app/default/home`

Công nghệ chính:

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Python 3.12+ |
| Test runner | `pytest` |
| Trình duyệt (UI) | `Playwright` (sync API), channel `chrome`/`msedge` |
| API client | `httpx` |
| Database | `psycopg` (PostgreSQL) |
| Cấu hình | `pydantic-settings` (đọc env + file `.env`) |
| Logging | `loguru` |
| Báo cáo | `pytest-html` (self-contained HTML) |
| Quản lý phụ thuộc | `uv` (theo `pyproject.toml`) |
| CI | Jenkins (`Jenkinsfile`) |

Mô hình thiết kế: **Page Object Model (POM)** + lớp tiện ích dùng chung (logging, reporting, config, db, api).

---

## 2. Cấu trúc thư mục

```text
src/automation/
├── config/      # Cấu hình tập trung (Settings)
├── logging/     # Logger + decorator log_method + step logs
├── reporting/   # Đính kèm (attachment) cho report
├── pages/       # Page Object Model
│   ├── base_page.py      # Lớp cha cho mọi page
│   ├── page_manager.py   # Khởi tạo lazy các page
│   ├── efms/efms_home_page.py
│   └── etms/etms_home_page.py
├── api/         # BaseApiClient (httpx)
└── db/          # Kết nối + thực thi truy vấn PostgreSQL

tests/
├── conftest.py  # Fixtures Playwright + hook report
├── ui/efms/     # Test UI eFMS (smoke + login)
├── ui/etms/     # Test UI eTMS (smoke + login)
├── api/         # (placeholder)
└── db/          # (placeholder)

reports/                       # Nơi xuất report.html của pytest-html
src/test/resources/report-html # Tài nguyên html/images cho report
```

---

## 3. Layer Config — `src/automation/config`

### `settings.py`

Định nghĩa class `Settings(BaseSettings)` (pydantic-settings). Mọi giá trị có thể bị **ghi đè bằng biến môi trường** hoặc file `.env` (`model_config = SettingsConfigDict(env_file=".env", ...)`). Quy ước: tên field `account_password` ↔ env `ACCOUNT_PASSWORD` (pydantic tự map theo tên, không phân biệt hoa thường).

Các field chính:

| Field | Mặc định | Ý nghĩa |
|---|---|---|
| `env` | `"UAT"` | Môi trường mục tiêu |
| `browser` | `"chrome"` | Trình duyệt: `chrome` hoặc `edge` |
| `browser_headless` | `False` | Mặc định mở trình duyệt có giao diện (headed) khi có display |
| `browser_timeout` | `30000` | Timeout mặc định (ms) cho thao tác Playwright + vòng chờ |
| `browser_slow_mo` | `0` | Làm chậm thao tác (ms) để debug |
| `viewport_width/height` | `1440 / 900` | Kích thước cửa sổ |
| `efms_base_url` / `etms_base_url` | URL UAT/staging | Trang chủ mỗi hệ thống |
| `api_base_url` | eFMS UAT | Base URL cho API client |
| `account_username` | `"henry.hieu"` | Tài khoản đăng nhập dùng chung |
| `account_password` | `None` (`repr=False`) | Mật khẩu — **không in ra log**, truyền lúc runtime |
| `db_url` / `db_username` / `db_password` | `None` | Thông tin DB (password ẩn `repr=False`) |
| `screenshot_dir` | `test-results/screenshots` | Nơi lưu ảnh khi test fail |
| `trace_dir` | `test-results/traces` | Nơi lưu trace |

**Method / thành phần:**

- `playwright_channel` (property): chuẩn hoá `browser` → channel của Playwright. `edge → "msedge"`, `chrome → "chrome"`; trình duyệt khác → raise `ValueError`.
- `get_settings()`: hàm có `@lru_cache` → trả về **một instance Settings duy nhất** (singleton, cache lại).
- `settings`: instance dùng sẵn cho toàn bộ project (`settings = get_settings()`).

> Lưu ý: vì `@lru_cache`, settings chỉ đọc env **một lần**. Test login lấy password qua `get_settings().account_password or os.getenv("ACCOUNT_PASSWORD")` để vẫn lấy được giá trị set sau khi import.

`__init__.py` export: `Settings`, `get_settings`, `settings`.

---

## 4. Layer Logging — `src/automation/logging`

### `logger.py`

- Tạo thư mục `logs/` nếu chưa có.
- Cấu hình `loguru.logger` ghi file `logs/automation.log` với `rotation="10 MB"`, `retention="10 days"`, `enqueue=True` (ghi log an toàn đa luồng/đa tiến trình).
- Export `logger` để các module khác dùng.

### `step_logger.py`

Quản lý **step log theo từng test** bằng `ContextVar` (`_step_logs`) — an toàn cho chạy song song (`pytest-xdist`).

| Hàm | Chức năng |
|---|---|
| `reset_step_logs()` | Đặt lại danh sách step log về rỗng (gọi đầu mỗi test) |
| `get_step_logs()` | Lấy bản sao danh sách step log hiện tại |
| `record_step_log(message)` | Thêm 1 dòng vào step log (nếu context đã được khởi tạo) |
| `safe_terminal_print(message)` | In ra stdout an toàn; nếu handle lỗi (Windows under pytest) thì fallback sang stderr rồi sang `logger.debug` |
| `log_method(step_name=None)` | **Decorator** quan trọng nhất (xem dưới) |

**`log_method`** — decorator bọc một method để tự động log:

1. Khi bắt đầu: ghi `[STEP START] <tên>` vào step log + `logger.info("START: ...")`.
2. Nếu chạy xong: ghi `[STEP PASS] <tên>` + `logger.success("PASS: ...")`, trả kết quả.
3. Nếu có exception: ghi `[STEP FAILED] <tên>` + `logger.exception("FAILED: ...")` rồi **re-raise** (không nuốt lỗi).

Tên step = `step_name` truyền vào, nếu không có thì dùng `func.__qualname__`. Dùng `@wraps` để giữ metadata gốc của hàm. Đây là cơ chế giúp report/terminal có dòng log "STEP START/PASS/FAILED" cho từng hành động.

`__init__.py` export: `get_step_logs`, `log_method`, `logger`, `reset_step_logs`, `safe_terminal_print`.

---

## 5. Layer Reporting — `src/automation/reporting`

### `attachments.py`

Lưu file đính kèm vào `test-results/attachments/` (tự tạo thư mục nếu chưa có).

| Hàm | Chức năng |
|---|---|
| `attach_text(name, content)` | Ghi nội dung text ra `<name>.txt` (UTF-8) |
| `attach_png(name, content)` | Ghi bytes ảnh ra `<name>.png` |
| `attach_file(path, name=None)` | Copy file từ `path` vào thư mục attachment; có thể đổi tên qua `name` |

Layer này dùng chung; ví dụ `BaseApiClient._request` gọi `attach_text("api-response", ...)` để lưu lại response của mỗi request.

### Phần report được lắp ráp ở đâu? → `tests/conftest.py`

Báo cáo HTML do **`pytest-html`** sinh ra (tham số `--html=reports/report.html --self-contained-html`). Việc làm giàu report nằm trong các hook của `conftest.py`:

- `pytest_configure`: tạo sẵn thư mục `reports/` và `screenshot_dir`.
- `pytest_runtest_setup`: gọi `reset_step_logs()` → mỗi test bắt đầu với step log rỗng.
- `pytest_runtest_makereport` (hookwrapper):
  - Chỉ xử lý giai đoạn `setup`/`call`; bỏ qua nếu `setup` đã pass.
  - Đính kèm dòng kết quả `extras.text("PASSED/FAILED/...: <nodeid>")` (mục **"Test result"**).
  - Nếu có step log → đính kèm **"Method logs"** vào report và gắn `report.method_logs`.
  - Ghi log tổng kết qua loguru (`success`/`warning`/`error`).
  - **Nếu test fail**: chụp **full-page screenshot** của `page` (fixture Playwright) lưu vào `screenshot_dir/<tên test>.png` rồi `extras.image(...)` đính ảnh vào report (mục **"Failure screenshot"**). Nếu không có fixture `page` → đính kèm note thay thế.
- `pytest_runtest_logreport`: in step log (`safe_terminal_print`) và dòng `[OUTCOME] nodeid` ra terminal → log hiển thị an toàn cả trên Windows.

**Vị trí output:**

```text
reports/report.html              # report HTML chính (self-contained)
test-results/screenshots/        # ảnh chụp khi fail
test-results/attachments/        # text/file đính kèm (vd: api-response.txt)
logs/automation.log              # log loguru
```

Thư mục `src/test/resources/report-html/{html,images}/` dùng để chứa tài nguyên report mẫu/asset (xem README trong thư mục đó).

---

## 6. Layer Pages (Page Object Model) — `src/automation/pages`

### `base_page.py` — `BasePage`

Lớp cha cho mọi page, bọc `playwright.sync_api.Page`.

| Thành phần | Chức năng |
|---|---|
| `__init__(page)` | Lưu đối tượng `Page` của Playwright |
| `current_url` (property) | Trả về `self.page.url` hiện tại |
| `open_url(url)` `@log_method` | `goto(url, wait_until="domcontentloaded")` rồi chờ DOM load |
| `wait_for_dom_content_loaded()` `@log_method` | Chờ trạng thái `domcontentloaded` |
| `wait_for_visible(selectors, element_name)` `@log_method` | Lặp tới khi tìm thấy phần tử **visible** trong danh sách `selectors` (poll mỗi 250ms tới `browser_timeout`); hết giờ → raise `AssertionError` mô tả `element_name` |
| `find_visible(selectors)` | Duyệt từng selector, trả về `Locator` đầu tiên **đang hiển thị**; không có → `None` |

Thiết kế dùng **danh sách selector dự phòng** (fallback) để bền với khác biệt DOM giữa các trang/môi trường.

### `page_manager.py` — `PageManager`

Điểm truy cập tập trung tới các page, khởi tạo **lazy** (chỉ tạo khi dùng lần đầu, sau đó cache):

| Thành phần | Chức năng |
|---|---|
| `__init__(page)` | Lưu `page`, đặt cache `_efms_home_page`/`_etms_home_page = None` |
| `efms_home_page` (property) | Tạo/cache & trả `EfmsHomePage(page)` |
| `etms_home_page` (property) | Tạo/cache & trả `EtmsHomePage(page)` |

Trong test dùng qua fixture `pages` (xem mục 8): `pages.efms_home_page`, `pages.etms_home_page`.

### `efms/efms_home_page.py` — `EfmsHomePage(BasePage)`

Thuộc tính class — các danh sách selector dự phòng:

- `username_selectors`: nhiều biến thể ô username/email/text.
- `password_selectors`: nhiều biến thể ô password.
- `submit_selectors`: nhiều biến thể nút Login/Sign in/submit.

Method:

| Method | `@log_method` | Chức năng |
|---|---|---|
| `open()` | "Open eFMS home page" | Mở `settings.efms_base_url`, trả về `self` (cho phép chaining) |
| `login(username, password)` | "Login to eFMS" | Chờ & điền username → password → click submit → chờ DOM load; trả `self` |
| `is_password_field_visible()` | "Check eFMS password field visible" | Trả `True/False` xem ô password còn hiển thị không (dùng để khẳng định đã đăng nhập) |

### `etms/etms_home_page.py` — `EtmsHomePage(BasePage)`

**Giống hệt cấu trúc `EfmsHomePage`** (cùng bộ selector dự phòng), chỉ khác:

| Method | `@log_method` | Khác biệt |
|---|---|---|
| `open()` | "Open eTMS home page" | Mở `settings.etms_base_url` |
| `login(username, password)` | "Login to eTMS" | Tương tự eFMS |
| `is_password_field_visible()` | "Check eTMS password field visible" | Tương tự eFMS |

> Hai page hiện chia sẻ logic gần như nhau qua `BasePage`; tách riêng để dễ mở rộng các thao tác đặc thù từng hệ thống về sau.

Các `__init__.py` của `pages`, `efms`, `etms` chỉ export class tương ứng (`PageManager`, `EfmsHomePage`, `EtmsHomePage`).

---

## 7. Layer API — `src/automation/api/base_api_client.py`

### `BaseApiClient`

Bọc `httpx.Client`.

| Method | `@log_method` | Chức năng |
|---|---|---|
| `__init__(base_url=None)` | – | Tạo `httpx.Client(base_url=base_url or settings.api_base_url, timeout=30)` |
| `get/post/put/patch/delete(path, **kwargs)` | "API GET/POST/..." | Gọi `_request` với method tương ứng |
| `close()` | – | Đóng client |
| `_request(method, path, **kwargs)` | "API request" | Thực hiện request, **đính kèm** `method path / status / body` vào report qua `attach_text("api-response", ...)`, trả `httpx.Response` |

Mọi response API được tự động lưu lại để phục vụ điều tra khi test fail.

---

## 8. Layer Database — `src/automation/db`

### `connection.py`

- `db_connection()` — context manager (`@contextmanager`): nếu chưa cấu hình `settings.db_url` → raise `RuntimeError("Set DB_URL ...")`; ngược lại mở `psycopg.connect(db_url, user=..., password=...)` và yield `Connection` (tự đóng khi thoát context).

### `query_executor.py` — `QueryExecutor`

| Method | `@log_method` | Chức năng |
|---|---|---|
| `fetch_all(sql, params=())` | "DB fetch all" | Chạy query, trả `list[dict]` (map theo tên cột từ `cursor.description`) |
| `fetch_one(sql, params=())` | "DB fetch one" | Gọi `fetch_all`, trả dòng đầu hoặc `None` |
| `execute(sql, params=())` | "DB execute" | Chạy lệnh ghi, `commit()`, trả `rowcount` |

Dùng tham số hoá (`params`) để tránh SQL injection; mọi truy vấn đều được log step.

---

## 9. Tests & Fixtures — `tests/`

### `conftest.py`

**Tuỳ chọn dòng lệnh** (`pytest_addoption`):

- `--browser` ∈ {`chrome`,`edge`}
- `--browser-headless` ∈ {`true`,`false`}

**Fixtures:**

| Fixture | Scope | Chức năng |
|---|---|---|
| `playwright_instance` | session | Khởi tạo `sync_playwright()` dùng chung |
| `browser` | function | Launch chromium với `channel` (chrome/msedge), `headless` (ưu tiên `--browser-headless` > settings), `slow_mo`; đóng sau test |
| `context` | function | `new_context` với viewport + `set_default_timeout(browser_timeout)` |
| `page` | function | Mở `new_page` từ context |
| `pages` | function | Trả `PageManager(page)` — dùng các page object |
| `account_password` | function | Lấy password từ settings/env; **nếu không có → `pytest.skip(...)`** nên login test tự bỏ qua an toàn |

**Hook report:** xem chi tiết tại mục 5.

### Các test hiện có

| File | Marker | Hành vi |
|---|---|---|
| `tests/ui/efms/test_efms_home.py::test_open_efms_home_page` | `smoke`, `efms` | Mở eFMS home → assert URL chứa `uat-efms.logtechub.com` |
| `tests/ui/etms/test_etms_home.py::test_open_etms_home_page` | `smoke`, `etms` | Mở eTMS home → assert URL chứa `staging-itllog-etms.logtechub.com` |
| `tests/ui/efms/test_efms_login.py::test_login_efms` | `login`, `efms` | Mở + login eFMS → assert ô password không còn hiển thị + URL đúng (cần `ACCOUNT_PASSWORD`) |
| `tests/ui/etms/test_etms_login.py::test_login_etms` | `login`, `etms` | Tương tự cho eTMS |

`tests/api`, `tests/db` hiện là placeholder (`.gitkeep`).

Markers khai báo trong `pyproject.toml`: `smoke`, `login`, `efms`, `etms`, `api`, `db` (chạy với `--strict-markers`).

---

## 10. Cách chạy & báo cáo

```bash
# Cài đặt (uv)
python3 -m pip install --user uv
uv sync --extra dev
uv run playwright install --with-deps chrome msedge

# Lint & type-check
uv run ruff check .
uv run pyright

# Smoke (mở home eFMS + eTMS)
uv run pytest -m smoke --browser chrome --browser-headless true

# Login (cần mật khẩu; bỏ qua nếu thiếu)
ACCOUNT_PASSWORD='<password>' uv run pytest -m login --browser chrome --browser-headless true

# Sinh report HTML self-contained
uv run pytest -m smoke --browser chrome --browser-headless true \
  --html=reports/report.html --self-contained-html
```

Report mở tại `reports/report.html`. Khi test fail sẽ có **ảnh chụp màn hình** và **Method logs** đính kèm trong report; log đầy đủ ở `logs/automation.log`.

---

## 11. Sơ đồ luồng (tóm tắt)

```text
pytest
 └─ conftest: reset step logs ─► fixtures (playwright→browser→context→page→pages)
      └─ Test gọi pages.<system>_home_page.open()/login()
            └─ BasePage.* (mỗi method bọc @log_method → ghi STEP START/PASS/FAILED)
      └─ makereport: gắn Test result + Method logs (+ Screenshot nếu fail)
 └─ pytest-html ─► reports/report.html
```
