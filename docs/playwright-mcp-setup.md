# Playwright MCP — Install & Usage Guide (Claude Code on Windows 11)

Ground truth verified against the official repo: `github.com/microsoft/playwright-mcp` (package `@playwright/mcp`, published by Microsoft).

Playwright MCP drives Chromium / Firefox / WebKit / MS Edge through Playwright's accessibility tree (structured, deterministic, non-vision). A `vision` capability is available if you need screenshot-based coordinates instead.

---

## 1. Prerequisites

| Requirement | Check command |
|---|---|
| **Node.js ≥ 20** (Playwright 1.55+ requires it) | `node --version` |
| **Claude Code** installed & authenticated | `claude --version` |
| **git-bash** (or any shell that runs `npx`) | — |
| **Disk space** for browser binaries (~400 MB for Chromium alone) | — |

If Node is older than 20, upgrade via `winget install OpenJS.NodeJS.LTS` or `nvm-windows`.

No need to install Chrome manually — Playwright ships its own Chromium. You can still target your installed Chrome/Edge via `--browser chrome` or `--browser msedge`.

---

## 2. Install (one command)

```bash
claude mcp add playwright --scope user -- npx @playwright/mcp@latest
```

- `playwright` is the server name used in `/mcp` output.
- `--scope user` installs it for all your projects (use `--scope project` to commit it to `.mcp.json` in this repo instead).
- `npx @playwright/mcp@latest` is the launch command — npm pulls the latest version automatically.
- Note the `--` separator before `npx`: it keeps the trailing flags attached to the server command instead of being parsed by `claude mcp add`.

---

## 3. Install browser binaries (first run only)

Playwright needs its browser binaries on disk. The easiest path: ask the agent to call the built-in `browser_install` tool once, or pre-install from the shell:

```bash
npx playwright install chromium         # minimal
# or
npx playwright install                  # all three engines
```

On Windows 11 behind a corporate proxy you may need:

```bash
set HTTPS_PROXY=http://your.proxy:port
npx playwright install chromium
```

---

## 4. Verify it loaded

Inside Claude Code:

```
/mcp
```

You should see `playwright` with status **connected**. First launch takes a few seconds while npx downloads the package.

---

## 5. Tools it exposes (core set)

| Category | Tools |
|---|---|
| **Navigation** | `browser_navigate`, `browser_navigate_back`, `browser_navigate_forward`, `browser_close`, `browser_wait_for` |
| **Snapshots & vision** | `browser_snapshot` (accessibility tree, preferred), `browser_take_screenshot` |
| **Input** | `browser_click`, `browser_type`, `browser_fill_form`, `browser_hover`, `browser_drag`, `browser_select_option`, `browser_press_key`, `browser_file_upload`, `browser_handle_dialog` |
| **Tabs** | `browser_tabs` (list/new/select/close) |
| **Page state** | `browser_evaluate`, `browser_console_messages`, `browser_network_requests`, `browser_resize` |
| **Install & output** | `browser_install`, `browser_pdf_save` |

`browser_snapshot` is the default way to understand the page — it returns the a11y tree with element refs. Prefer it over `browser_take_screenshot` unless you genuinely need pixels.

---

## 6. Common configuration flags

Pass flags after the command. Example — headless Chromium with an isolated profile:

```bash
claude mcp remove playwright --scope user
claude mcp add playwright --scope user -- npx @playwright/mcp@latest --headless --isolated
```

Useful flags:

| Flag | Purpose |
|---|---|
| `--browser <chromium\|chrome\|msedge\|firefox\|webkit>` | Pick engine / installed browser |
| `--headless` | Run without a window (CI-friendly) |
| `--isolated` | Use a throwaway profile per session |
| `--user-data-dir <path>` | Reuse a persistent profile (e.g. for logged-in sessions) |
| `--executable-path <path>` | Point to a specific browser binary |
| `--device <name>` | Emulate a Playwright device (e.g. `"iPhone 15"`) |
| `--viewport-size <w,h>` | Fix viewport size, e.g. `1920,1080` |
| `--ignore-https-errors` | Accept self-signed certs |
| `--proxy-server <url>` | Route traffic through a proxy |
| `--allowed-origins <list>` | Semicolon-separated allow-list of origins |
| `--blocked-origins <list>` | Semicolon-separated deny-list |
| `--save-trace` | Record a Playwright trace (`.zip`) for each session |
| `--output-dir <dir>` | Where screenshots, traces, and PDFs land |
| `--caps <name,name>` | Enable optional capability sets (e.g. `vision`, `pdf`) |
| `--config <path>` | Load a JSON config file instead of CLI flags |
| `--port <n>` | Expose the server over HTTP/SSE instead of stdio |

Full, current flag list: `npx @playwright/mcp@latest --help`.

---

## 7. Example prompts to try

```
Open http://localhost:8080/api/v1/clients with Playwright and list the network requests.
```

```
Navigate to https://example.com, take an accessibility snapshot, and summarize the page structure.
```

```
Open http://localhost:8080/swagger-ui/index.html, expand the POST /api/v1/clients operation, click "Try it out", fill the body with sample data, and submit it. Report the response status and body.
```

```
Go to https://news.ycombinator.com, click the first story, and save the article page as a PDF.
```

```
Emulate an iPhone 15, open https://example.com, and take a screenshot.
```

---

## 8. Windows 11 gotchas

- **Browser not installed**: first run may fail with `Executable doesn't exist at ...`. Fix with `npx playwright install chromium` (or call the `browser_install` MCP tool).
- **Corporate SSL proxy**: set `HTTPS_PROXY` before installing binaries, and pass `--proxy-server` + `--ignore-https-errors` at runtime.
- **Path separators**: use forward slashes in bash — e.g. `--user-data-dir "C:/Users/frsavard/pw-mcp-profile"`.
- **Antivirus delay**: Defender can slow the first Chromium launch considerably. Wait before retrying.
- **Persistent login** across runs: prefer `--user-data-dir <path>` over `--isolated`.
- **Multiple MCP browsers**: running Chrome DevTools MCP and Playwright MCP in the same session works fine — they use separate browser instances. Just pick the right tool when driving the agent (`browser_*` for Playwright, everything else for Chrome DevTools).

---

## 9. Accessibility-first vs. vision mode

Default behaviour returns the accessibility tree — each element has a `ref` you pass back to `browser_click`, `browser_type`, etc. This is fast, deterministic, and token-cheap.

Enable vision mode only when the page is genuinely inaccessible (canvas UIs, image maps):

```bash
claude mcp add playwright --scope user -- npx @playwright/mcp@latest --caps vision
```

Vision tools operate by pixel coordinates from `browser_take_screenshot`.

---

## 10. Removing or reconfiguring

```bash
claude mcp list                                 # see what's installed
claude mcp remove playwright --scope user
claude mcp add playwright --scope user -- npx @playwright/mcp@latest --headless --browser chromium
```

To switch to a project-scoped install (committed to `.mcp.json` in this repo):

```bash
claude mcp remove playwright --scope user
claude mcp add playwright --scope project -- npx @playwright/mcp@latest
```

---

## 11. References

- Repo: https://github.com/microsoft/playwright-mcp
- npm: https://www.npmjs.com/package/@playwright/mcp
- Playwright docs: https://playwright.dev
- MCP spec: https://modelcontextprotocol.io
