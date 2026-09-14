# Commerce deployment (port of upstream `docs/deployment.md`)

## Provider switching (no code changes)

```env
AI_PROVIDER=openrouter
AI_MODEL=anthropic/claude-sonnet-4-5-20250929
OPENROUTER_API_KEY=...
```

`AnthropicProvider` speaks the native Messages API; everything else goes
through `OpenAiCompatibleProvider` (`/chat/completions`). Gemini uses its
OpenAI-compatible endpoint. `AI_FALLBACKS="deepseek:deepseek-chat,..."` adds
ordered single-try fallbacks.

## Run paths

Upstream defines three runtimes; this deployment maps them as:

| Upstream path | Here |
|---|---|
| Messages API turn loop | `CommerceAgent` / `MerchantAgent` over `AiProvider` |
| Agent SDK console | Not ported (no Java SDK loop); `scripts/smoke-ai.sh` is the console equivalent for smoke tests |
| Managed Agents + MCP server | `/mcp` JSON-RPC endpoint + `agent.yaml`-style config in `app.properties`; `MerchantAgent.digest()` is the scheduled-digest body — call `/admin/ai-merchant/digest` from cron/scheduler |

## Managed-agents notes

- Manifests upstream declare tools one by one and mount the MCP server
  beside the agent. Here the tool surface is already config-driven
  (`MERCHANT_ENABLE_*`), and `/mcp` serves `tools/list` / `tools/call`.
- On a hosted-agent platform, set `MERCHANT_REQUIRE_HOST_APPROVAL=false`
  only if the platform's own approval prompt fronts `apply` — otherwise keep
  the default `true`.

## Production checklist

1. Set only the provider key you use; keep all keys in env/secrets, never in
   frontend or git.
2. Tune merchant guardrails from demonstration defaults.
3. Put auth + TLS + rate limits in front of `/ai-support/*`, `/admin/*`, `/mcp`.
4. Schedule the digest (`GET /admin/ai-merchant/digest` with an admin
   session, or wire `MerchantAgent.digest()` into your scheduler).
5. Retention: `AI_MEMORY_RETENTION_DAYS`; wire memory purge into account deletion.
6. Cost: each agent step is one billed call, bounded by `AI_MAX_TOOL_STEPS`.
