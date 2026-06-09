import allure
import pytest

from automation.config import settings


@allure.epic("eTMS")
@allure.feature("Login")
@pytest.mark.login
@pytest.mark.etms
def test_login_etms(pages, account_password: str):
    pages.etms_home_page.open().login(settings.account_username, account_password)
    assert not pages.etms_home_page.is_password_field_visible()
    assert "staging-itllog-etms.logtechub.com" in pages.etms_home_page.current_url
