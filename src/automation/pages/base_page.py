import time

from playwright.sync_api import Locator, Page

from automation.config import settings


class BasePage:
    def __init__(self, page: Page):
        self.page = page

    @property
    def current_url(self) -> str:
        return self.page.url

    def open_url(self, url: str) -> None:
        self.page.goto(url, wait_until="domcontentloaded")
        self.wait_for_dom_content_loaded()

    def wait_for_dom_content_loaded(self) -> None:
        self.page.wait_for_load_state("domcontentloaded")

    def wait_for_visible(self, selectors: list[str], element_name: str) -> Locator:
        deadline = time.monotonic() + settings.browser_timeout / 1000
        while time.monotonic() < deadline:
            locator = self.find_visible(selectors)
            if locator is not None:
                return locator
            self.page.wait_for_timeout(250)
        raise AssertionError(f"Could not find visible {element_name} within {settings.browser_timeout} ms")

    def find_visible(self, selectors: list[str]) -> Locator | None:
        for selector in selectors:
            locator = self.page.locator(selector)
            if locator.count() > 0:
                first = locator.first
                if first.is_visible():
                    return first
        return None
