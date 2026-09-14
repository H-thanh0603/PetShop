# Commerce Agent Platform (provider-agnostic)

PetShop's AI shopping assistant, built from the architecture of
[anthropics/commerce-agents](https://github.com/anthropics/commerce-agents)
but **not coupled to Anthropic**. All commerce logic talks to
`services.ai.AiProvider`; providers are adapters behind that interface.

Full-port map (every upstream module has a counterpart here):

| Upstream | Here |
|---|---|
| `shopping-agent` (backend, tools, executor, gates, skills) | `PetShopCommerceBackend`, `CommerceTools`, `CommerceAgent`, 5 skills in `commerce-skills/shopping/` |
| `merchant-agent` (backend, changes, gates, analysis, skills) | `services.ai.merchant.*`, 5 skills in `commerce-skills/merchant/`, approval UI at `Admin > AI Merchant` |
| `commerce-common` (fencing, memory, grounding, presentation, events, MCP) | `services.ai.common.*` (Fence, MemoryService, Cards, AppEventBus), `/mcp` endpoint |
| `managed-agents` manifests + scheduled digest | config-driven tool surface, `MerchantAgent.digest()` + `/admin/ai-merchant/digest` |
| `runtime-agent-sdk` consoles | `scripts/smoke-ai.sh` |
| `plugins/commerce-builder` | `docs/commerce-builder.md` checklist |
| `docs/` (safety, backends, deployment) | `docs/commerce-safety.md`, `commerce-backends.md`, `commerce-deployment.md` |
| 4 example verticals | not copied — PetShop itself is the storefront; see `commerce-backends.md` |

```
JSP chat UI ──► UserAiSupportServlet (/ai-support/chat, /ai-support/stream)
                      │  (session auth, persistence, escalation — unchanged)
                      ▼
              DeepSeekService (compat façade, same AiResponse API)
                      ▼
              CommerceAgent (agent runtime: skills prompt + tool loop + gates)
                      ▼
              AiProvider (interface: complete / stream)
                      ▼
        ┌─────────────┬──────────────┬─────────────┬──────────────┐
        │ Anthropic   │ OpenRouter   │ TokenRouter │ OpenAI/Gemini│
        │ (native     │ (OpenAI-     │ (OpenAI-     │ /DeepSeek    │
        │ Messages)   │ compat)      │ compat)     │ (OpenAI-compat)
        └─────────────┴──────────────┴─────────────┴──────────────┘
```

## What was taken from Commerce Agents (adapted, not copied)

| Commerce Agents concept | PetShop equivalent |
|---|---|
| `StorefrontBackend` interface | `services.ai.PetShopCommerceBackend` (search, details, recommend, orders, policies — server-side, session-scoped) |
| Tool contracts | `services.ai.CommerceTools`: `searchProducts`, `getProductDetails`, `compareProducts`, `recommendProducts`, `getOrderStatus`, `searchPolicies` |
| Executor + turn loop | `services.ai.CommerceAgent`: up-to-`AI_MAX_TOOL_STEPS` tool iterations, then a forced final answer |
| Skills (`shopping-agent/skills/`) | Prompt sections: product_discovery, product_details, comparison, recommendations, order_support, policy_faq |
| Fencing | Model sees only tool results + policy text; never credentials, never other users' rows |
| Provenance gates | `relatedProductIds`/`relatedOrderId` accepted only if returned by a tool this session |
| Guest rule | Deterministic code guard (no model call) refuses guest order inquiries |
| Merchant approval gate | N/A — the agent is read-only plus cart navigation; no order placement, no money movement, same guarantee as upstream `checkout` handoff |

What was deliberately **not** ported: the Python runtimes, merchant agent
(listing/pricing/campaign writes — PetShop has no such flows), MCP servers.

## Configuration

All in env / system properties / `app.properties` (via `Util.AppConfig`).
Keys stay server-side; the frontend only ever calls `/ai-support/*`.

```env
AI_PROVIDER=openrouter
AI_MODEL=anthropic/claude-sonnet-4-5-20250929
OPENROUTER_API_KEY=...
# optional: ordered fallbacks, each tried at most once
AI_FALLBACKS=deepseek:deepseek-chat,anthropic:claude-haiku-4-5-20251001
AI_TIMEOUT_SECONDS=30
AI_MAX_TOOL_STEPS=6
```

Per-provider keys: `ANTHROPIC_API_KEY`, `OPENROUTER_API_KEY`,
`TOKENROUTER_API_KEY`, `OPENAI_API_KEY`, `GOOGLE_API_KEY`, `DEEPSEEK_API_KEY`
(+ `*_BASE_URL` overrides; defaults point at each provider's standard endpoint).
Legacy `DEEPSEEK_*` keys still work when `AI_*` are unset.

### Switch examples (no code changes)

```env
# Anthropic native
AI_PROVIDER=anthropic
AI_MODEL=claude-sonnet-4-5-20250929
ANTHROPIC_API_KEY=...

# DeepSeek
AI_PROVIDER=deepseek
AI_MODEL=deepseek-chat
DEEPSEEK_API_KEY=...

# OpenAI
AI_PROVIDER=openai
AI_MODEL=gpt-4o-mini
OPENAI_API_KEY=...
```

## Supported providers

| Provider | Adapter | Notes |
|---|---|---|
| Anthropic | `AnthropicProvider` (native Messages API) | OpenAI-shaped tools converted to `tools`/`tool_use`; system prompt mapped to `system` |
| OpenRouter | `OpenAiCompatibleProvider` | `provider/model` prefix stripped only for native calls; OpenRouter receives the full id |
| TokenRouter | `OpenAiCompatibleProvider` | Same as above |
| OpenAI | `OpenAiCompatibleProvider` | |
| Gemini | `OpenAiCompatibleProvider` | Via Gemini's OpenAI-compat endpoint |
| DeepSeek | `OpenAiCompatibleProvider` | Default; back-compat with previous behavior |

Capability differences are handled by `ProviderCapabilities` + the default
`stream()` fallback (single call when a provider lacks SSE). Missing keys,
401/403, 404 (model), 429, 5xx are classified by `AiException` and drive the
fallback chain instead of failing unpredictably.

## Add a new provider

1. If it speaks `/chat/completions`: only add `*_API_KEY` / `*_BASE_URL` cases
   to `AiConfig` — no new class needed.
2. If it has a native protocol: implement `AiProvider` (see `AnthropicProvider`
   as the example), register it in `AiProviderFactory.create`.
3. Document key + default model here.

## Add a new commerce tool

1. Add a `ToolExecutor` in `CommerceTools.all()` backed by a
   `PetShopCommerceBackend` method (server-side, session-scoped, validated).
2. Keep results ≤ 12 000 chars and sanitized (`CommerceTools.sanitize`).
3. Mention it in the `CommerceAgent` system prompt skill list.
4. No provider code changes — adapters convert the schema automatically.

## Add a new agent/skill

Add a prompt section in `CommerceAgent.buildSystemPrompt()` plus backing tools.
Keep multi-step order enforcement in the backend (per upstream `docs/backends.md`),
not in the prompt.

## API

- `POST /ai-support/chat` — `{message, sessionId?}` → `{sessionId, answer,
  intent, needAdminSupport, relatedProducts, relatedOrder, provider, model}`
- `POST /ai-support/stream` — same input, SSE: `delta` events then `done`
  (currently streams the post-tool-loop answer in chunks; token-level streaming
  exists at the `AiProvider.stream` layer for a future upgrade).
- `GET /ai-support/history|messages|unread-count` — unchanged.

## Observability

Logs (never keys/PII beyond ids): provider, model, requestId, step, tool name,
latencyMs, prompt/completion tokens, fallback engagements, parse errors.

## Security

Keys server-side only; session ownership checks on every endpoint; tool input
validation; provenance gate on ids; guest order guard; product/policy text
sanitized before entering prompts; LLM output validated as JSON and never
executed as logic; agent cannot place orders or move money.

## Testing

- `services.ai.AiProviderAbstractionTest` (offline, 8 tests): prefix stripping,
  error classification, missing-key behavior, fallback parsing, capabilities,
  guest guard, tool safety, sanitizer.
- Live provider check: set `AI_PROVIDER`/`AI_MODEL`/key and exercise
  `/ai-support/chat` ("Mèo con nên ăn gì?", "so sánh 2 sản phẩm…", order
  question logged-in vs guest). Repeat per provider; agent code is untouched
  between runs by design.
- Existing `DeepSeekServiceTest` remains valid (façade API unchanged).

## Limitations

- SSE `/ai-support/stream` chunks the final answer; per-token streaming during
  the tool loop is not yet wired to the frontend.
- Models that ignore tool calls degrade to context-only answers (previous
  behavior); structured-output parse failures return a safe admin-handoff message.
- `AI_MAX_TOOL_STEPS` bounds cost; each step is one billed provider call.
