# Agentic Readiness Checklist — đánh giá trên code thật

Ngày đánh giá: 2026-09-14. Thang: ✅ đạt / 🟡 một phần / 🔴 chưa đạt.
Mọi nhận định đều trỏ vào file/hàm cụ thể; các gap tìm được trong đợt này
đã được fix (ghi rõ commit nội dung).

## Kết quả tổng hợp

| # | Tiêu chí | Mức | Bằng chứng |
|---|---|---|---|
| 1 | Goal understanding | 🟡 | System prompt + 10 skills định nghĩa mục tiêu từng flow (`SkillLoader`, `commerce-skills/`). Hiểu goal vẫn phụ thuộc model làm theo prompt — không có intent classifier cứng. Giảm rủi ro bằng cấu trúc JSON bắt buộc + intent enum. |
| 2 | Planning | 🟡 | Skill `planning-goals` tách nhu cầu → gọi tool từng nhu cầu. Không có plan object duyệt trước khi chạy (không cần cho read-only shopping; merchant change nào cũng qua approve). |
| 3 | Tool calling | ✅ | Vòng lặp tool qua `AiProvider` trung lập; adapter OpenAI-compat + Anthropic native chuyển schema (`OpenAiCompatibleProvider`, `AnthropicProvider`). Test: `AiProviderAbstractionTest`. |
| 4 | Tool permission | ✅ | Tool surface sinh từ config (`MerchantConfig` switches); tên lạ bị từ chối (`MerchantTools.execute`, `CommerceTools.execute`); merchant/stage tools yêu cầu session admin (`McpServlet`, `/admin/*` + `AuthorizationFilter`). |
| 5 | Multi-step execution | ✅ | Loop tối đa `AI_MAX_TOOL_STEPS`, hết ngân sách thì ép câu trả lời cuối không tool (`CommerceAgent`, `MerchantAgent`). |
| 6 | State management | ✅ (đã fix) | **Gap đã fix:** provenance shopping chỉ sống trong 1 turn; ledger merchant mỗi instance một bản (stage nơi này, apply nơi khác fail) + reset số thứ tự sau restart. Fix: `SessionStateStore` (provenance cross-turn, cap 100, hết hạn 2h, key `chat:{id}`); ledger static dùng chung + hydrate từ DB qua `reattach` (giữ high-water sequence). Test: `crossTurnProvenanceSurvivesAcrossToolInstances`, `ledgerReattachPreservesSequence`. |
| 7 | Memory | ✅ | `MemoryStore`/`DbMemoryStore` (`ai_customer_memory`), write filter key≤64/value≤200/category enum/chặn identifier (`MemoryService.validateFact`), extraction post-turn chỉ đọc user+assistant text, lifecycle xem/xóa ở `/admin/ai-merchant/memory`. |
| 8 | Context management | ✅ (đã fix) | Lịch sử 10 tin/turn; **thêm** `capPrompt` (`AI_MAX_PROMPT_CHARS=12000`, bỏ tin cũ nhất, giữ system + câu hỏi hiện tại). Char-based thay vì token-based (ghi nhận). |
| 9 | Observation loop | ✅ | Kết quả tool append vào messages mỗi step; held/failure trả về dạng result thường, exception tool không bao giờ kết thúc turn. |
| 10 | Error recovery | ✅ | Fallback chain (`AiProviderFactory.completeWithFallback`, mỗi ứng viên thử 1 lần, không loop vô hạn); parse JSON lỗi → thông điệp an toàn + chuyển admin; tool lỗi → error result. |
| 11 | Retry / timeout | 🟡 | Timeout `AI_TIMEOUT_SECONDS` mọi call; phân loại retryable (`AiException`). **Không retry cùng provider** — quyết định có chủ ý để chặn chi phí, retry xuyên-provider qua fallback. Ghi nhận là giới hạn. |
| 12 | Human approval | ✅ | `MERCHANT_REQUIRE_HOST_APPROVAL=true` mặc định; apply chỉ chạy khi đã mark duyệt trên mặt approval (`AdminMerchantAgentServlet` approve → apply tách 2 bước; chat duyệt không có tác dụng); guardrails check lại lúc apply theo config hiện hành. |
| 13 | Prompt injection defense | ✅ (đã gia cố) | Instruction hierarchy rule 0 trong prompt; `Fence.sanitize` gỡ control chars + forged turn markers + fence giả; guest-order guard cứng không qua model; policy/order chỉ từ tool. Test: `forgedTurnMarkersStrippedFromUntrustedText`, `guestOrderGuardCoversAdminInjectionPhrasing`. Dư: đánh giá trên model thật cần chạy eval live. |
| 14 | Tool injection defense | ✅ | `execute()` chỉ chạy đúng tên được yêu cầu, args parse an toàn, kết quả tool là data (không bao giờ `eval`/dispatch tiếp). Test: `toolResultCannotBecomeInstruction`, args 50k chars không vỡ. |
| 15 | Data isolation | ✅ | Ownership check mọi endpoint chat; guest không đọc order (2 lớp: guard + backend); query order scoped theo user; memory subject theo user; merchant/MCP-write yêu cầu admin. Không có user/merchant id trong tool args. |
| 16 | Audit logs | ✅ (đã fix) | **Gap đã fix:** trước đây chỉ log text. Thêm bảng `ai_agent_audit` (V6) + `AuditLog.record`: mọi tool call, turn_complete, approve/apply/discard, MCP call — gồm actor, session, provider/model/requestId/latency. Best-effort, không chặn turn. |
| 17 | Cost control | ✅ (đã fix) | **Gap đã fix:** chỉ có max steps/tokens. Thêm quota ngày theo user (`AI_MAX_TURNS_PER_DAY=100`, `countUserMessagesToday`) + cap tin nhắn/session (`AI_MAX_MSGS_PER_SESSION=60`, `countBySession`), HTTP 429 khi vượt. Token usage log mỗi step. |
| 18 | Rate limiting | ✅ | `RateLimitFilter`: chat/stream 10 rpm, `/mcp` 30, **thêm** `/admin/ai-merchant/chat` 30. Lưu ý: merchant routes dựa thêm vào auth admin. |
| 19 | Agent-to-agent readiness | ✅ (đã fix) | **Gap đã fix:** chưa có handoff. Thêm: escalation shopping → event `support_escalation` vào `merchant:queue`; digest merchant liệt kê; endpoint `/admin/ai-merchant/escalations`; `requestId` trả về trong chat JSON để trace liên-agent; `/mcp` là machine interface chung (JSON-RPC, schema chuẩn, không cần state ẩn). |

## Gap còn lại (ghi nhận, chưa fix)

1. **Retry cùng provider**: không có (chủ ý, chặn chi phí). Nếu cần: thêm 1 retry cho lỗi TIMEOUT ở read tools.
2. **Token-based compaction**: `capPrompt` đếm chars, không đếm tokens chính xác theo từng model.
3. **Eval live**: các test injection mới là unit-level; cần chạy prompt tấn công thật qua nhiều provider/model (dùng `scripts/smoke-ai.sh` + key thật).
4. **Plan object duyệt trước**: không có client-side plan review cho shopping (chấp nhận được vì agent read-only + escalation).
5. **A2A protocol đầy đủ** (như ACP/A2A chuẩn): hiện tại là event bus nội bộ + MCP JSON-RPC, chưa phải chuẩn liên-agent bên ngoài.

## Cách chạy lại đánh giá

```sh
./gradlew test --tests "services.ai.*"   # 26 tests: abstraction (8) + full port (9) + adversarial (9)
sh scripts/smoke-ai.sh                   # live smoke từng provider (cần key + login cookie)
```
