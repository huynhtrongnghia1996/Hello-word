from pathlib import Path

import allure


def attach_text(name: str, content: str) -> None:
    allure.attach(content, name=name, attachment_type=allure.attachment_type.TEXT)


def attach_png(name: str, content: bytes) -> None:
    allure.attach(content, name=name, attachment_type=allure.attachment_type.PNG)


def attach_file(path: str | Path, name: str | None = None) -> None:
    allure.attach.file(str(path), name=name or Path(path).name)
