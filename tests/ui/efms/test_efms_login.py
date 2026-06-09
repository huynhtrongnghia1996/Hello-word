import pytest

from automation.config import settings


@pytest.mark.login
@pytest.mark.efms
def test_login_efms(pages, account_password: str):
    pages.efms_home_page.open().login(settings.account_username, account_password)
    assert not pages.efms_home_page.is_password_field_visible()
    assert "uat-efms.logtechub.com" in pages.efms_home_page.current_url
