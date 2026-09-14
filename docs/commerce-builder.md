# Commerce builder (port of the `commerce-builder` plugin concept)

Upstream ships a Claude Code plugin that scaffolds agents against your
systems. In this repo, scaffolding a new agent/skill/flow is a checklist
instead of a plugin:

## Add a skill (new flow)

1. Create `src/main/resources/commerce-skills/<role>/<skill-name>/SKILL.md`
   with `When to use` / `Flow` / `Rules` sections.
2. Register the name in `SkillLoader.ROLE_SKILLS` — it loads automatically
   into that role's system prompt. No other code changes.
3. Add backing tools first if the flow needs data the current tools lack
   (see below).

## Add a commerce tool

1. Implement the read/write on `PetShopCommerceBackend` (shopping) or
   `PetShopMerchantBackend` (merchant) — server-side, session-scoped.
2. Add a `ToolExecutor` in `CommerceTools.all()` / `MerchantTools.all()`
   with an OpenAI-shaped schema; adapters convert it per provider.
3. Writes must be stage-only (merchant) or non-existent (shopping);
   mention the tool in the relevant SKILL.md.

## Add an agent

1. New role = new package under `services.ai` + skills directory +
   runtime class following `MerchantAgent` (tool loop + prompt + gates).
2. New servlet + routes in `WebRegistrationConfig` + rate limits.
3. Document it in `docs/commerce-agent.md` and the safety table.

## Review an agent (port of `/review-commerce-agent`)

- [ ] Every write passes a provenance gate + guardrails + approval?
- [ ] Backend returns notes, never invented zeros?
- [ ] No credential, token, or other-user data reaches the model?
- [ ] Tool surface is config-driven; unknown names refused?
- [ ] New provider works with zero agent-code changes (run the smoke script)?
