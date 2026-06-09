import allure
import pytest


@allure.epic("eFMS")
@allure.feature("Home Page")
@pytest.mark.smoke
@pytest.mark.efms
def test_open_efms_home_page(pages):
    pages.efms_home_page.open()
    assert "uat-efms.logtechub.com" in pages.efms_home_page.current_url
