# Bao cao thiet ke Framework Automation Test - Python

## Stack

Python 3.12, pytest, Playwright Python, pytest-html, httpx, psycopg, pydantic-settings, loguru, ruff, pyright, Git, Jenkins.

## 1. Muc tieu framework

- Xay dung framework automation test dung Python thay cho Java/Maven/TestNG.
- Ho tro UI automation cho eFMS va eTMS bang Playwright Python.
- Ho tro API automation bang httpx va database validation bang psycopg/PostgreSQL.
- Ho tro pytest-html report, screenshot khi fail, logging va Jenkins CI/CD.
- Giam boilerplate code, tang toc do viet test va debug cho team QA Automation.

## 2. Kien truc tong the

- Test Layer: pytest tests, markers smoke/login/efms/etms/api/db.
- Fixture Layer: browser, context, page va PageManager trong `tests/conftest.py`.
- Page Object Layer: `EfmsHomePage`, `EtmsHomePage`, `BasePage`, `PageManager`.
- Service Layer: `BaseApiClient`, `QueryExecutor`, settings, logging, reporting helpers.
- Execution Layer: pytest CLI, Jenkins parameters, pytest-html report va Playwright artifacts.

## 3. Cau truc project

```text
src/automation
|-- api
|-- config
|-- db
|-- logging
|-- pages
|   |-- base_page.py
|   |-- page_manager.py
|   |-- efms/efms_home_page.py
|   `-- etms/etms_home_page.py
`-- reporting

tests
|-- conftest.py
|-- ui
|   |-- efms
|   `-- etms
|-- api
`-- db
```

## 4. Thiet ke UI Automation

- Dung Playwright Python sync API de code de doc va debug nhanh.
- Browser/context/page duoc quan ly bang pytest fixtures.
- Page Object tach theo app: eFMS va eTMS khong dung login page chung.
- Method login trong tung page se cho username textbox, password textbox va login button visible truoc khi fill/click.
- Headed/headless dieu khien bang `BROWSER_HEADLESS` hoac `--browser-headless`.

## 5. Thiet ke API Automation

- Dung httpx cho API client.
- `BaseApiClient` chuan hoa GET/POST/PUT/PATCH/DELETE.
- Response duoc ghi vao `test-results/attachments` de debug nhanh.
- Co the mo rong them auth client, schema validation va contract tests.

## 6. Thiet ke Database Layer

- Dung psycopg cho PostgreSQL.
- `QueryExecutor` ho tro fetch_all, fetch_one va execute.
- DB credentials lay tu environment variables, khong commit vao Git.
- Chi dung DB validation khi can xac nhan data backend hoac setup/cleanup data.

## 7. Config va Secret

- Dung pydantic-settings trong `src/automation/config/settings.py`.
- Config mac dinh:
  - `EFMS_BASE_URL=https://uat-efms.logtechub.com/en/#/home`
  - `ETMS_BASE_URL=https://staging-itllog-etms.logtechub.com/en/#/app/default/home`
  - `ACCOUNT_USERNAME=henry.hieu`
- Password truyen bang `ACCOUNT_PASSWORD` hoac `.env`, khong hardcode.

## 8. Reporting va Logging

- Dung pytest-html de sinh `reports/report.html`.
- Screenshot duoc attach khi test fail.
- Dung loguru ghi log file `logs/automation.log`.
- Playwright artifacts luu o `test-results/screenshots` va `test-results/attachments`.

## 9. Pytest Execution Strategy

- Dung markers thay cho TestNG XML:
  - `smoke`
  - `login`
  - `efms`
  - `etms`
  - `api`
  - `db`
- Chay song song bang pytest-xdist khi can:

```bash
uv run pytest -m smoke -n auto
```

## 10. Jenkins Pipeline

- Jenkins pipeline gom cac stage:
  - Checkout
  - Install dependencies bang uv
  - Install Playwright browsers
  - Quality check bang ruff va pyright
  - Run pytest
  - Generate pytest-html report
  - Archive artifacts

## 11. Tool de code, debug va maintain

- Cursor hoac VS Code: code nhanh, AI assist, debug Python.
- PyCharm Professional: debug pytest va database tot neu team co license.
- Playwright Inspector: `PWDEBUG=1`.
- Trace viewer: `playwright show-trace trace.zip`.
- Ruff: lint va format.
- Pyright: type check.
- pre-commit: chan code xau truoc khi commit.
- DBeaver/DataGrip: debug PostgreSQL.

## 12. Roadmap trien khai

- Phase 1: tao Python project voi pyproject, pytest, settings, Playwright fixtures.
- Phase 2: migrate Page Object eFMS/eTMS va smoke tests.
- Phase 3: migrate login tests, API client, DB query executor.
- Phase 4: tich hop pytest-html, screenshot, Jenkins pipeline.
- Phase 5: them lint/type check/pre-commit va regression suite.
