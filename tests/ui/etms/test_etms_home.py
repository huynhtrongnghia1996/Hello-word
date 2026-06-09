import allure
import pytest


@allure.epic("eTMS")
@allure.feature("Home Page")
@pytest.mark.smoke
@pytest.mark.etms
def test_open_etms_home_page(pages):
    pages.etms_home_page.open()
    assert "staging-itllog-etms.logtechub.com" in pages.etms_home_page.current_url
