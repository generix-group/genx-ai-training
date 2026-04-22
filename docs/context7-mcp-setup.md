# Context7 MCP — Install & Use

Context7 is an MCP server (by Upstash) that fetches **up-to-date, version-accurate
documentation** for public libraries (Spring, Next.js, React, Supabase, etc.) and
injects it into the LLM's context. It replaces guess-work / stale training data
with the real current docs.

It exposes two tools to the model:

| Tool | Purpose |
|---|---|
| `resolve-library-id` | Turn a free-text library name into a Context7 library id (e.g. `spring-projects/spring-boot` → `/spring-projects/spring-boot`). |
| `get-library-docs` (a.k.a. `query-docs`) | Fetch focused doc snippets for a library id + a query. |

Two ways to run it:

1. **Remote HTTP** — `https://mcp.context7.com/mcp` (recommended, no local Node process).
2. **Local stdio** — `npx -y @upstash/context7-mcp` (works offline for the spawner, but still calls out to context7.com).

---

## 1. Prerequisites

1. **Node.js ≥ 18** on PATH (only required for the local stdio transport).
   Verify:
   ```bash
   node -v
   npx -v
   ```
2. **A Context7 API key** (free tier available).
   - Sign up at https://context7.com/dashboard and copy the key.
   - Without a key the endpoint still works but is heavily rate-limited and
     unauthenticated requests are de-prioritised.
3. **An MCP-capable client**: Claude Code CLI, Claude Desktop, Cursor, VS Code
   (GitHub Copilot / MCP preview), or Windsurf.

---

## 2. Fastest path — one-shot installer

The upstream repo ships an installer that handles OAuth, generates a key, and
wires up the selected client:

```bash
npx ctx7 setup --claude      # for Claude Code / Claude Desktop
npx ctx7 setup --cursor      # for Cursor
npx ctx7 setup --opencode    # for OpenCode
```

To remove it later:

```bash
npx ctx7 remove
```

If you want full control instead, follow the manual steps below for your client.

---

## 3. Claude Code CLI (recommended on this machine)

### 3a. Remote HTTP transport (preferred)

```bash
claude mcp add \
  --scope user \
  --transport http \
  --header "CONTEXT7_API_KEY: YOUR_API_KEY" \
  context7 \
  https://mcp.context7.com/mcp
```

- `--scope user` writes to your user-level MCP config so it's available in every
  project. Use `--scope project` to scope it to this repo only (commits a
  `.mcp.json`).
- Replace `YOUR_API_KEY` with the value from your Context7 dashboard.

### 3b. Local stdio transport (fallback)

```bash
claude mcp add \
  --scope user \
  context7 \
  -- npx -y @upstash/context7-mcp --api-key YOUR_API_KEY
```

On Windows, if `npx` isn't found by the spawned process, wrap it with `cmd /c`:

```bash
claude mcp add --scope user context7 -- cmd /c npx -y @upstash/context7-mcp --api-key YOUR_API_KEY
```

### 3c. Verify

```bash
claude mcp list
```

You should see `context7` with status `connected`. Inside a Claude Code session,
the tools appear as `mcp__context7__resolve-library-id` and
`mcp__context7__get-library-docs`.

### 3d. Remove

```bash
claude mcp remove context7 --scope user
```

---

## 4. Other clients

### Claude Desktop

**Remote:** Settings → Connectors → *Add Custom Connector* →
name `Context7`, URL `https://mcp.context7.com/mcp`.

**Local stdio:** edit `claude_desktop_config.json` (Settings → Developer → Edit
Config):

```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp", "--api-key", "YOUR_API_KEY"]
    }
  }
}
```

### Cursor

Edit `%USERPROFILE%\.cursor\mcp.json` (Windows) or `~/.cursor/mcp.json`:

**Remote:**
```json
{
  "mcpServers": {
    "context7": {
      "url": "https://mcp.context7.com/mcp",
      "headers": { "CONTEXT7_API_KEY": "YOUR_API_KEY" }
    }
  }
}
```

**Local:**
```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp", "--api-key", "YOUR_API_KEY"]
    }
  }
}
```

### VS Code (MCP / Copilot)

Create `.vscode/mcp.json` in the workspace:

**Remote:**
```json
{
  "servers": {
    "context7": {
      "type": "http",
      "url": "https://mcp.context7.com/mcp",
      "headers": { "CONTEXT7_API_KEY": "YOUR_API_KEY" }
    }
  }
}
```

**Local:**
```json
{
  "servers": {
    "context7": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp", "--api-key", "YOUR_API_KEY"]
    }
  }
}
```

### Windsurf

```json
{
  "mcpServers": {
    "context7": {
      "serverUrl": "https://mcp.context7.com/mcp",
      "headers": { "CONTEXT7_API_KEY": "YOUR_API_KEY" }
    }
  }
}
```

### Windows + npx gotcha (applies to all stdio configs)

If the client can't find `npx`, wrap the command with `cmd /c`:

```json
{
  "mcpServers": {
    "context7": {
      "command": "cmd",
      "args": ["/c", "npx", "-y", "@upstash/context7-mcp", "--api-key", "YOUR_API_KEY"]
    }
  }
}
```

---

## 5. Using Context7 from the LLM

You generally **don't call the tools by hand**. You just tell the model to
consult Context7 in natural language. The easiest trigger is the literal phrase
**`use context7`** anywhere in your prompt.

### Good prompts

- `Implement a Spring WebFlux global exception handler. use context7`
- `Create a Next.js 14 middleware that checks a JWT. use context7`
- `Show the Supabase email/password sign-up API. use library /supabase/supabase`
- `How do I pin Hibernate 6.5 to validate mode only? use context7`

### What happens under the hood

1. Model calls `resolve-library-id` with the library you mentioned.
2. Context7 returns one or more matching ids (e.g. `/spring-projects/spring-framework`).
3. Model calls `get-library-docs` with that id + your question.
4. Context7 returns curated doc snippets.
5. Model uses those snippets — not its training data — to answer.

### Pinning a specific library

If the auto-resolve picks the wrong library, give it the id directly:

```
use library /spring-projects/spring-boot — explain @ConditionalOnProperty
```

### Pinning a version

Context7 accepts versioned ids where the library publishes them:

```
How do I configure springdoc 2.5 with WebFlux? use context7
```

---

## 6. Fit with this project

In this repo (Spring Boot 3.5 / Java 21 / WebFlux not used, but MapStruct +
Lombok + Flyway + springdoc-openapi), Context7 is most useful for:

- Spring Boot 3.5 idioms (config properties, actuator, Flyway integration).
- Hibernate 6.x behaviour (e.g. `ddl-auto: validate`, second-level cache).
- MapStruct 1.5+ (component model, `@AfterMapping`, lombok-mapstruct-binding).
- springdoc-openapi annotations and `@ApiResponses` for new endpoints.
- Jackson / JSON Merge Patch (RFC 7386) semantics when touching
  `JsonMergePatchUtils`.

Tip: pair it with the existing `rag` skill — `rag` already targets
Spring / Hibernate / Postgres / Jackson / RFC docs; Context7 broadens that to
any library the MCP knows about.

---

## 7. Troubleshooting

| Symptom | Fix |
|---|---|
| `claude mcp list` shows `context7: failed` with no detail | Run `claude mcp get context7` for the error. For stdio, test the command standalone: `npx -y @upstash/context7-mcp --api-key KEY` and check it prints MCP handshake JSON. |
| `401` / `403` from the remote endpoint | Wrong or missing `CONTEXT7_API_KEY` header. Re-add with the correct key. |
| Rate-limit `429` | You're on the anonymous tier. Add an API key, or wait. |
| `npx` not found on Windows | Use the `cmd /c npx ...` form shown in §4. |
| Tools not called even with `use context7` | Confirm the server is `connected` (`claude mcp list`) and that your model actually has tool-use enabled. Re-open the session after adding the MCP. |
| Corporate proxy blocks `mcp.context7.com` | Switch to the local stdio transport, or set `HTTPS_PROXY` in the environment before launching the client. |

---

## 8. References

- Upstream repo: https://github.com/upstash/context7
- Dashboard / API keys: https://context7.com/dashboard
- Full client matrix (30+ clients): https://context7.com/docs/resources/all-clients
- Remote MCP endpoint: `https://mcp.context7.com/mcp`
- npm package (local stdio): `@upstash/context7-mcp`
