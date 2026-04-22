# Chrome DevTools MCP — Install & Usage Guide (Claude Code on Windows 11)

Ground truth verified against the official repo: `github.com/ChromeDevTools/chrome-devtools-mcp`.

---

## 1. Prerequisites

| Requirement | Check command |
|---|---|
| **Node.js ≥ 20.19** (latest maintenance LTS) | `node --version` |
| **Google Chrome** installed (stable, beta, canary, or dev) | Launch Chrome once manually |
| **Claude Code** installed & authenticated | `claude --version` |
| **git-bash** | — |

If Node is older than 20.19, upgrade via `winget install OpenJS.NodeJS.LTS` or `nvm-windows`.

---

## 2. Install (one command)

```bash
claude mcp add chrome-devtools --scope user npx chrome-devtools-mcp@latest
```

- `chrome-devtools` is the server name used in `/mcp` output.
- `--scope user` installs it for all your projects (use `--scope project` to commit it to `.mcp.json` in this repo instead).
- `npx chrome-devtools-mcp@latest` is the launch command — npm pulls the latest version automatically.

---

## 3. Verify it loaded

Inside Claude Code:

```
/mcp
```

You should see `chrome-devtools` with status **connected**. First launch takes a few seconds while npx downloads the package and Chrome boots.

---

## 4. Tools it exposes (34 total)

| Category | Tools |
|---|---|
| **Navigation** | `navigate_page`, `new_page`, `close_page`, `list_pages`, `select_page`, `wait_for` |
| **Input** | `click`, `drag`, `fill`, `fill_form`, `hover`, `type_text`, `press_key`, `upload_file`, `handle_dialog` |
| **Debugging** | `take_screenshot`, `take_snapshot`, `evaluate_script`, `list_console_messages`, `get_console_message`, `lighthouse_audit` |
| **Network** | `list_network_requests`, `get_network_request` |
| **Performance** | `performance_start_trace`, `performance_stop_trace`, `performance_analyze_insight` |
| **Emulation** | `emulate`, `resize_page` |
| **Extensions** | `install_extension`, `list_extensions`, `reload_extension`, `trigger_extension_action`, `uninstall_extension` |
| **Memory** | `take_memory_snapshot` |

---

## 5. Common configuration flags

Pass flags after the command if you need them. Example — headless + isolated profile:

```bash
claude mcp remove chrome-devtools --scope user
claude mcp add chrome-devtools --scope user -- npx chrome-devtools-mcp@latest --headless --isolated
```

Useful flags:

| Flag | Purpose |
|---|---|
| `--headless` | Run Chrome without a UI (faster, CI-friendly) |
| `--isolated` | Use a throwaway profile per session |
| `--channel stable\|beta\|canary\|dev` | Pick Chrome channel |
| `--executablePath <path>` | Point to a specific Chrome binary |
| `--userDataDir <path>` | Reuse a persistent profile (e.g. for logged-in sessions) |
| `--viewport 1920x1080` | Fix viewport size |
| `--proxyServer <url>` | Route traffic through a proxy (corporate networks) |
| `--logFile <path>` | Dump server logs for troubleshooting |

Full list: `--autoConnect`, `--browserUrl/-u`, `--wsEndpoint/-w`, `--wsHeaders`, `--acceptInsecureCerts`, `--chromeArg`, `--ignoreDefaultChromeArg`, `--redactNetworkHeaders`, `--slim`, plus experimental flags (`--experimentalVision`, `--experimentalScreencast`, `--experimentalWebmcp`, `--experimentalFfmpegPath`).

---

## 6. Example prompts to try

```
Open http://localhost:8080/api/v1/clients in Chrome, then list the network requests.
```

```
Navigate to https://example.com, take a screenshot, and show me the console messages.
```

```
Run a Lighthouse audit on http://localhost:8080 and summarize the performance issues.
```

```
Start a performance trace, navigate to https://news.ycombinator.com, stop the trace, and analyze the insights.
```

```
Open http://localhost:8080/swagger-ui/index.html, fill the client POST form with sample data, and submit it.
```

---

## 7. Windows 11 gotchas

- **Chrome not found**: if the server can't locate Chrome, pass `--executablePath "C:/Program Files/Google/Chrome/Application/chrome.exe"` (forward slashes in bash).
- **Slow first launch**: Chrome cold-starts can take 10–20 s on Windows. Wait before retrying. If `/mcp` repeatedly times out, relaunch Claude Code.
- **Corporate SSL proxy**: add `--acceptInsecureCerts` and/or `--proxyServer http://your.proxy:port`.
- **Keep a logged-in session** across runs: use `--userDataDir "C:/Users/frsavard/chrome-mcp-profile"` instead of `--isolated`.
- **Don't run headed Chrome in CI**: use `--headless`.

---

## 8. Removing or reconfiguring

```bash
claude mcp list                         # see what's installed
claude mcp remove chrome-devtools --scope user
claude mcp add chrome-devtools --scope user -- npx chrome-devtools-mcp@latest --headless
```

---

## 9. References

- Repo: https://github.com/ChromeDevTools/chrome-devtools-mcp
- npm: https://www.npmjs.com/package/chrome-devtools-mcp
