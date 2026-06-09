from __future__ import annotations

import os
import sys
from collections.abc import Generator
from pathlib import Path
from typing import Any, cast

import pytest
from playwright.sync_api import Browser, BrowserContext, Page, Playwright, sync_playwright
from pytest_html import extras

from automation.config import get_settings, settings
from automation.logging import get_step_logs, logger, reset_step_logs
from automation.pages import PageManager


def pytest_addoption(parser: pytest.Parser) -> None:
    parser.addoption("--browser", choices=["chrome", "edge"], default=None)
    parser.addoption("--browser-headless", choices=["true", "false"], default=None)


def pytest_configure(config: pytest.Config) -> None:
    del config
    Path("reports").mkdir(exist_ok=True)
    Path(settings.screenshot_dir).mkdir(parents=True, exist_ok=True)


@pytest.fixture(scope="session")
def playwright_instance() -> Generator[Playwright, None, None]:
    with sync_playwright() as playwright:
        yield playwright


@pytest.fixture()
def browser(
    pytestconfig: pytest.Config,
    playwright_instance: Playwright,
) -> Generator[Browser, None, None]:
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
def context(browser: Browser) -> Generator[BrowserContext, None, None]:
    context = browser.new_context(
        viewport={"width": settings.viewport_width, "height": settings.viewport_height}
    )
    context.set_default_timeout(settings.browser_timeout)
    yield context
    context.close()


@pytest.fixture()
def page(context: BrowserContext) -> Generator[Page, None, None]:
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


def pytest_runtest_setup(item: pytest.Item) -> None:
    del item
    reset_step_logs()


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item: pytest.Item, call: pytest.CallInfo):
    outcome = yield
    report = outcome.get_result()

    if report.when not in {"setup", "call"}:
        return
    if report.when == "setup" and report.passed:
        return

    status = report.outcome.upper()
    message = f"{status}: {item.nodeid}"
    report.extras = getattr(report, "extras", [])
    report.extras.append(extras.text(message, name="Test result"))
    method_logs = get_step_logs()
    if method_logs:
        report.extras.append(extras.text("\n".join(method_logs), name="Method logs"))
        for line in method_logs:
            print(line, file=sys.__stdout__, flush=True)

    if report.passed:
        logger.success(message)
    elif report.skipped:
        logger.warning(message)
    else:
        logger.error(message)

    if not report.failed:
        return

    page = cast(Any, item).funcargs.get("page")
    if page is None:
        report.extras.append(
            extras.text("No Playwright page fixture available", name="Failure note")
        )
        return

    screenshot_dir = Path(settings.screenshot_dir)
    screenshot_dir.mkdir(parents=True, exist_ok=True)
    screenshot_path = screenshot_dir / f"{item.name}.png"
    page.screenshot(path=screenshot_path, full_page=True)

    report.extras.append(extras.image(str(screenshot_path), name="Failure screenshot"))


def pytest_runtest_logreport(report: pytest.TestReport) -> None:
    if report.when not in {"setup", "call"}:
        return
    if report.when == "setup" and report.passed:
        return
    print(f"[{report.outcome.upper()}] {report.nodeid}")
