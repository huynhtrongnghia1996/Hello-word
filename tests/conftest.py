from __future__ import annotations

import os
from pathlib import Path

import allure
import pytest
from playwright.sync_api import Browser, BrowserContext, Page, Playwright, sync_playwright

from automation.config import get_settings, settings
from automation.pages import PageManager


def pytest_addoption(parser: pytest.Parser) -> None:
    parser.addoption("--browser", choices=["chrome", "edge"], default=None)
    parser.addoption("--browser-headless", choices=["true", "false"], default=None)


@pytest.fixture(scope="session")
def playwright_instance() -> Playwright:
    with sync_playwright() as playwright:
        yield playwright


@pytest.fixture()
def browser(pytestconfig: pytest.Config, playwright_instance: Playwright) -> Browser:
    browser_name = pytestconfig.getoption("--browser") or settings.browser
    headless_option = pytestconfig.getoption("--browser-headless")
    headless = settings.browser_headless if headless_option is None else headless_option == "true"
    channel = "msedge" if browser_name == "edge" else "chrome"
    browser = playwright_instance.chromium.launch(
        channel=channel,
        headless=headless,
        slow_mo=settings.browser_slow_mo,
    )
    yield browser
    browser.close()


@pytest.fixture()
def context(browser: Browser) -> BrowserContext:
    context = browser.new_context(viewport={"width": settings.viewport_width, "height": settings.viewport_height})
    context.set_default_timeout(settings.browser_timeout)
    yield context
    context.close()


@pytest.fixture()
def page(context: BrowserContext) -> Page:
    page = context.new_page()
    yield page


@pytest.fixture()
def pages(page: Page) -> PageManager:
    return PageManager(page)


@pytest.fixture()
def account_password() -> str:
    password = get_settings().account_password or os.getenv("ACCOUNT_PASSWORD")
    if not password:
        pytest.skip("Set ACCOUNT_PASSWORD to run login tests")
    return password


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item: pytest.Item, call: pytest.CallInfo):
    outcome = yield
    report = outcome.get_result()
    if report.when != "call" or not report.failed:
        return

    page = item.funcargs.get("page")
    if page is None:
        return

    screenshot_dir = Path(settings.screenshot_dir)
    screenshot_dir.mkdir(parents=True, exist_ok=True)
    screenshot_path = screenshot_dir / f"{item.name}.png"
    screenshot = page.screenshot(path=screenshot_path, full_page=True)
    allure.attach(screenshot, name="Failure screenshot", attachment_type=allure.attachment_type.PNG)
