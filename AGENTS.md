# AGENTS.md

## Cursor Cloud specific instructions

This repo is a Python (3.12+) Playwright/pytest automation framework for eFMS/eTMS. Dependencies are managed by `uv` via `pyproject.toml`. Standard commands live in `README.md`; detailed architecture/page/method/report docs live in `docs/framework-documentation.md`.

Non-obvious caveats for running here:

- **Use `--browser chrome` in the cloud.** Google Chrome is already installed system-wide and Playwright's `chrome` channel works. Microsoft Edge (`msedge`) is generally not available on this Linux VM, so `--browser edge` will fail.
- **Always run headless** (`--browser-headless true`) — the VM has no display by default.
- **Smoke/login tests hit live external sites** (`uat-efms.logtechub.com`, `staging-itllog-etms.logtechub.com`), so they require outbound internet. They are not hermetic; a failure may be an upstream/site issue rather than a code regression.
- **Login tests need `ACCOUNT_PASSWORD`** (env or secret). Without it, the `login`-marked tests `pytest.skip` safely (see `account_password` fixture in `tests/conftest.py`). The shared username defaults to `henry.hieu`.
- **DB helpers need `DB_URL`** (+ optional `DB_USERNAME`/`DB_PASSWORD`); `db_connection()` raises `RuntimeError` if `DB_URL` is unset.
- **CI gates are `ruff check` + `pyright` only** (see `Jenkinsfile`); `ruff format` is not enforced, so do not reformat existing files just to satisfy a format check.
- Settings are cached via `@lru_cache` (`get_settings`), so env vars set after import are read at call time only where code re-reads them (e.g. the password fixture).
