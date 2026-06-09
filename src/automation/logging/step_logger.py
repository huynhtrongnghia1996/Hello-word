from collections.abc import Callable
from functools import wraps
from typing import ParamSpec, TypeVar

from automation.logging.logger import logger

P = ParamSpec("P")
R = TypeVar("R")


def log_method(step_name: str | None = None) -> Callable[[Callable[P, R]], Callable[P, R]]:
    def decorator(func: Callable[P, R]) -> Callable[P, R]:
        @wraps(func)
        def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
            name = step_name or func.__qualname__
            logger.info("START: {}", name)
            try:
                result = func(*args, **kwargs)
                logger.success("PASS: {}", name)
                return result
            except Exception:
                logger.exception("FAILED: {}", name)
                raise

        return wrapper

    return decorator
