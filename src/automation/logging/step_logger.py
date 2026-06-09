import sys
from collections.abc import Callable
from contextvars import ContextVar
from functools import wraps
from typing import ParamSpec, TypeVar

from automation.logging.logger import logger

P = ParamSpec("P")
R = TypeVar("R")
_step_logs: ContextVar[list[str] | None] = ContextVar("step_logs", default=None)


def reset_step_logs() -> None:
    _step_logs.set([])


def get_step_logs() -> list[str]:
    return list(_step_logs.get() or [])


def record_step_log(message: str) -> None:
    logs = _step_logs.get()
    if logs is not None:
        logs.append(message)
    print(message, file=sys.__stdout__, flush=True)


def log_method(step_name: str | None = None) -> Callable[[Callable[P, R]], Callable[P, R]]:
    def decorator(func: Callable[P, R]) -> Callable[P, R]:
        @wraps(func)
        def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
            name = step_name or func.__qualname__
            record_step_log(f"[STEP START] {name}")
            logger.info("START: {}", name)
            try:
                result = func(*args, **kwargs)
                record_step_log(f"[STEP PASS] {name}")
                logger.success("PASS: {}", name)
                return result
            except Exception:
                record_step_log(f"[STEP FAILED] {name}")
                logger.exception("FAILED: {}", name)
                raise

        return wrapper

    return decorator
