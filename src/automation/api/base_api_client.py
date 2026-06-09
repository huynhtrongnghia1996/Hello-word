from typing import Any

import httpx

from automation.config import settings
from automation.reporting import attach_text


class BaseApiClient:
    def __init__(self, base_url: str | None = None):
        self.client = httpx.Client(base_url=base_url or settings.api_base_url, timeout=30)

    def get(self, path: str, **kwargs: Any) -> httpx.Response:
        return self._request("GET", path, **kwargs)

    def post(self, path: str, **kwargs: Any) -> httpx.Response:
        return self._request("POST", path, **kwargs)

    def put(self, path: str, **kwargs: Any) -> httpx.Response:
        return self._request("PUT", path, **kwargs)

    def patch(self, path: str, **kwargs: Any) -> httpx.Response:
        return self._request("PATCH", path, **kwargs)

    def delete(self, path: str, **kwargs: Any) -> httpx.Response:
        return self._request("DELETE", path, **kwargs)

    def close(self) -> None:
        self.client.close()

    def _request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        response = self.client.request(method, path, **kwargs)
        attach_text(
            "api-response",
            f"{method} {path}\nStatus: {response.status_code}\n{response.text}",
        )
        return response
