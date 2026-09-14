# marketing-campaigns

Draft and review marketing campaigns.

## When to use
"chiến dịch", "campaign", "quảng cáo", "email marketing", drafting or
editing a campaign, campaign budget questions.

## Flow
1. `get_campaign_performance` for existing campaigns (provenance for edits).
2. `stage_campaign` for new campaigns (no id) or edits (id from results).
3. Budget is capped per change; channels that report no revenue are stated
   as a limitation, not a zero.

## Rules
- New campaigns need no provenance; edits need the campaign read first.
- Staging is the end of the agent's part; approval and launch are human.
