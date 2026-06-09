# De xuat chuyen Automation Framework tu Java sang Python

Tai lieu nay dinh nghia cach chuyen framework hien tai tu Java/Maven/TestNG sang Python theo huong de code, debug va maintain lau dai.

## 1. Muc tieu khi chuyen sang Python

- Giam boilerplate code so voi Java.
- Viet test nhanh hon, doc de hon cho QA Automation.
- Debug UI/API/DB truc tiep, nhanh va it cau hinh hon.
- Van giu du nang luc hien tai: UI, API, PostgreSQL, pytest-html report, logging, Jenkins, Git.
- Ho tro eFMS va eTMS rieng biet, dung chung account/config khi can.

## 2. Stack Python de xuat

| Nhu cau | Java hien tai | Python de xuat | Ly do |
| --- | --- | --- | --- |
| Language | Java 17 | Python 3.12 | On dinh, ecosystem test tot, type hint tot |
| Build/dependency | Maven | uv + pyproject.toml | Nhanh, lock dependency tot, de dung trong CI |
| Test runner | TestNG | pytest | Fixture manh, plugin nhieu, syntax gon |
| UI automation | Playwright Java | Playwright Python | API tuong duong, trace/debug rat tot |
| API automation | Rest Assured | httpx hoac requests | De viet client, support sync/async, debug request nhanh |
| Database | PostgreSQL JDBC | psycopg + SQLAlchemy Core optional | Native PostgreSQL, query ro rang |
| Report | TestNG/Java report | pytest-html + pytest-metadata | Nhe, mot file HTML, khong can Java/report CLI rieng |
| Logging | Log4j2 | logging/loguru | Don gian, de format va ghi file |
| Config | properties | pydantic-settings + .env/yaml | Validate config, doc ro, override bang env |
| Parallel | TestNG parallel | pytest-xdist | Chay song song bang `-n auto` |
| Retry | RetryAnalyzer | pytest-rerunfailures | Gon, config theo command |
| Code quality | Maven plugins | ruff + mypy/pyright + pre-commit | Lint/format/type check nhanh |

Khuyen nghi chon:

```text
Python 3.12
uv
pytest
playwright
httpx
psycopg[binary]
pytest-html
pytest-metadata
pydantic-settings
ruff
pyright hoac mypy
pytest-xdist
pytest-rerunfailures
pre-commit
```

## 3. Cau truc project Python de xuat

```text
automation-framework-python
|-- pyproject.toml
|-- uv.lock
|-- pytest.ini
|-- Jenkinsfile
|-- README.md
|-- .env.example
|-- config
|   |-- uat.yaml
|   `-- staging.yaml
|-- src
|   `-- automation
|       |-- api
|       |   |-- base_api_client.py
|       |   `-- auth_client.py
|       |-- config
|       |   `-- settings.py
|       |-- db
|       |   |-- connection.py
|       |   `-- query_executor.py
|       |-- logging
|       |   `-- logger.py
|       |-- pages
|       |   |-- base_page.py
|       |   |-- page_manager.py
|       |   |-- efms
|       |   |   `-- efms_home_page.py
|       |   `-- etms
|       |       `-- etms_home_page.py
|       `-- reporting
|           `-- attachments.py
`-- tests
    |-- conftest.py
    |-- ui
    |   |-- efms
    |   |   |-- test_efms_home.py
    |   |   `-- test_efms_login.py
    |   `-- etms
    |       |-- test_etms_home.py
    |       `-- test_etms_login.py
    |-- api
    `-- db
```

## 4. Define config toi uu

Nen dung `pydantic-settings` de config co validation va override duoc bang environment variable.

Vi du `.env.example`:

```text
ENV=UAT
BROWSER=chrome
BROWSER_HEADLESS=false
ACCOUNT_USERNAME=henry.hieu
ACCOUNT_PASSWORD=
EFMS_BASE_URL=https://uat-efms.logtechub.com/en/#/home
ETMS_BASE_URL=https://staging-itllog-etms.logtechub.com/en/#/app/default/home
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

Vi du `settings.py`:

```python
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    env: str = "UAT"
    browser: str = "chrome"
    browser_headless: bool = False
    browser_timeout: int = 30_000

    efms_base_url: str
    etms_base_url: str

    account_username: str = "henry.hieu"
    account_password: str | None = None

    db_url: str | None = None
    db_username: str | None = None
    db_password: str | None = None

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
```

Luu y:

- Khong commit password that vao Git.
- Password nen lay tu Jenkins Credentials hoac environment variable.
- Config URL tach ro `efms_base_url` va `etms_base_url`.

## 5. Define fixture Playwright voi pytest

Trong Python, nen de browser/page lifecycle o `conftest.py`.

```python
import pytest
from playwright.sync_api import sync_playwright

from automation.config.settings import settings
from automation.pages.page_manager import PageManager


@pytest.fixture(scope="session")
def playwright_instance():
    with sync_playwright() as playwright:
        yield playwright


@pytest.fixture()
def page(playwright_instance):
    browser = playwright_instance.chromium.launch(
        channel="msedge" if settings.browser == "edge" else "chrome",
        headless=settings.browser_headless,
        slow_mo=0,
    )
    context = browser.new_context(
        viewport={"width": 1440, "height": 900},
    )
    context.set_default_timeout(settings.browser_timeout)
    page = context.new_page()
    yield page
    context.close()
    browser.close()


@pytest.fixture()
def pages(page):
    return PageManager(page)
```

## 6. Define Page Object

Theo yeu cau hien tai: khong tao page login chung. Login nam trong tung page eFMS/eTMS.

Vi du `efms_home_page.py`:

```python
from playwright.sync_api import Page, expect

from automation.config.settings import settings
from automation.pages.base_page import BasePage


class EfmsHomePage(BasePage):
    USERNAME_INPUT = "input[name='username'], input[type='text']"
    PASSWORD_INPUT = "input[name='password'], input[type='password']"
    LOGIN_BUTTON = "button[type='submit'], button:has-text('Login')"

    def open(self) -> "EfmsHomePage":
        self.page.goto(settings.efms_base_url, wait_until="domcontentloaded")
        return self

    def login(self, username: str, password: str) -> "EfmsHomePage":
        username_input = self.page.locator(self.USERNAME_INPUT).first
        password_input = self.page.locator(self.PASSWORD_INPUT).first
        login_button = self.page.locator(self.LOGIN_BUTTON).first

        expect(username_input).to_be_visible(timeout=settings.browser_timeout)
        username_input.fill(username)

        expect(password_input).to_be_visible(timeout=settings.browser_timeout)
        password_input.fill(password)

        expect(login_button).to_be_visible(timeout=settings.browser_timeout)
        login_button.click()
        self.page.wait_for_load_state("domcontentloaded")
        return self
```

`EtmsHomePage` co cung pattern nhung dung `settings.etms_base_url`.

## 7. Define PageManager

```python
from playwright.sync_api import Page

from automation.pages.efms.efms_home_page import EfmsHomePage
from automation.pages.etms.etms_home_page import EtmsHomePage


class PageManager:
    def __init__(self, page: Page):
        self.page = page
        self._efms_home_page: EfmsHomePage | None = None
        self._etms_home_page: EtmsHomePage | None = None

    @property
    def efms_home_page(self) -> EfmsHomePage:
        if self._efms_home_page is None:
            self._efms_home_page = EfmsHomePage(self.page)
        return self._efms_home_page

    @property
    def etms_home_page(self) -> EtmsHomePage:
        if self._etms_home_page is None:
            self._etms_home_page = EtmsHomePage(self.page)
        return self._etms_home_page
```

## 8. Define test style voi pytest

```python
import pytest
from automation.config.settings import settings


@pytest.mark.smoke
@pytest.mark.efms
def test_open_efms_home_page(pages):
    pages.efms_home_page.open()
    assert "uat-efms.logtechub.com" in pages.efms_home_page.current_url


@pytest.mark.login
@pytest.mark.efms
def test_login_efms(pages):
    if not settings.account_password:
        pytest.skip("Set ACCOUNT_PASSWORD to run login tests")

    pages.efms_home_page.open().login(
        settings.account_username,
        settings.account_password,
    )
```

## 9. Tool de code, debug, maintain de nhat

### Code editor

- Cursor hoac VS Code: phu hop neu team da dung AI coding/debug.
- PyCharm Professional: manh cho debug Python, pytest, database tools.

Khuyen nghi:

- Team QA automation: Cursor/VS Code + Python extension + Playwright extension.
- Team can debug sau bang breakpoint: PyCharm Professional hoac Cursor/VS Code voi `debugpy`.

### Debug UI Playwright

Dung cac tool sau:

```bash
PWDEBUG=1 pytest tests/ui/efms/test_efms_login.py --headed -s
pytest tests/ui --headed --slowmo 500 -s
playwright show-trace trace.zip
```

Nen bat khi debug:

- Screenshot on failure.
- Video on failure.
- Playwright trace on failure.
- pytest-html report link toi screenshot, trace, request/response artifacts.

### Debug API

- httpx event hooks hoac logging request/response.
- pytest `-s` de xem log runtime.
- Ghi request/response JSON vao `test-results/attachments`.
- Postman/Bruno/Insomnia de manual reproduce nhanh.

### Debug DB

- DBeaver hoac DataGrip de xem PostgreSQL.
- psycopg query executor chi nen dung parameterized query.

### Maintain code

Dung bat buoc:

```text
ruff        lint + format
pyright     type check nhanh
pytest      test runner
pre-commit  chan code xau truoc khi commit
pytest-html  HTML report
```

Vi du command:

```bash
uv run ruff check .
uv run ruff format .
uv run pyright
uv run pytest -m smoke -n auto
uv run pytest -m login --headed
uv run pytest -m smoke --html=reports/report.html --self-contained-html
```

## 10. Jenkins pipeline Python de xuat

```groovy
pipeline {
    agent any

    parameters {
        choice(name: 'ENV', choices: ['UAT'], description: 'Target environment')
        choice(name: 'BROWSER', choices: ['chrome', 'edge'], description: 'Browser channel')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Headless mode')
        choice(name: 'MARKER', choices: ['smoke', 'login', 'regression'], description: 'Pytest marker')
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Install') {
            steps {
                sh 'uv sync'
                sh 'uv run playwright install --with-deps chrome msedge'
            }
        }

        stage('Quality') {
            steps {
                sh 'uv run ruff check .'
                sh 'uv run pyright'
            }
        }

        stage('Test') {
            steps {
                withCredentials([string(credentialsId: 'automation-account-password', variable: 'ACCOUNT_PASSWORD')]) {
                    sh '''
                        ENV=${ENV} BROWSER=${BROWSER} BROWSER_HEADLESS=${HEADLESS} \
                        uv run pytest -m ${MARKER} -n auto --html=reports/report.html --self-contained-html
                    '''
                }
            }
        }

    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**/*,test-results/**/*', allowEmptyArchive: true
        }
    }
}
```

## 11. Migration mapping tu Java sang Python

| Java hien tai | Python tuong ung |
| --- | --- |
| `pom.xml` | `pyproject.toml` + `uv.lock` |
| TestNG XML suite | pytest markers + command line |
| `BaseUiTest` | pytest fixtures trong `conftest.py` |
| `PlaywrightManager` | pytest fixture `page`, `context`, `browser` |
| `PageManager.java` | `page_manager.py` |
| `EfmsHomePage.java` | `efms_home_page.py` |
| `EtmsHomePage.java` | `etms_home_page.py` |
| `ConfigManager.java` | `settings.py` dung pydantic-settings |
| `QueryExecutor.java` | `query_executor.py` dung psycopg |
| `BaseApiClient.java` | `base_api_client.py` dung httpx |
| Java attachment helper | helper ghi artifact vao `test-results/attachments` |
| `RetryAnalyzer` | `pytest-rerunfailures` |

## 12. Khuyen nghi cuoi cung

Neu doi sang Python, nen define framework theo cac contract sau:

1. Config phai typed va validate bang `pydantic-settings`.
2. Browser/page lifecycle nam trong pytest fixtures, khong tao browser trong test.
3. Page Object tach theo app: `efms`, `etms`; khong dung login chung neu UI/login flow co kha nang khac nhau.
4. Secret khong commit vao Git; lay tu environment/Jenkins Credentials.
5. Moi action UI quan trong phai `expect(...).to_be_visible()` truoc khi fill/click.
6. Bat pytest-html screenshot/trace/video artifacts khi fail.
7. Bat `ruff`, `pyright`, `pre-commit` trong CI de code maintain de hon.

De toi uu nhat cho team QA automation, stack nen la:

```text
Python 3.12 + uv + pytest + Playwright Python + pytest-html + httpx + psycopg + pydantic-settings + ruff + pyright + pre-commit + Jenkins
```
