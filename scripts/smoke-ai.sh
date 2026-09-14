#!/bin/sh
# Smoke test for the commerce agent platform (port of scripts/smoke_chat.py).
# Usage: BASE=http://localhost:8080/PetShop sh scripts/smoke-ai.sh
# Requires: logged-in session cookie for order questions; admin cookie for merchant.
BASE="${BASE:-http://localhost:8080/PetShop}"
JAR="${COOKIE_JAR:-/tmp/petshop-cookies.txt}"

say() { printf '\n==> %s\n' "$1"; }

say "1. shopping chat (product advice)"
curl -s -c "$JAR" -b "$JAR" -X POST "$BASE/ai-support/chat" \
  -H 'Content-Type: application/json' \
  -d '{"message":"Mèo con nên ăn pate gì?"}' | head -c 600; echo

say "2. shopping stream (SSE)"
curl -s -c "$JAR" -b "$JAR" -N -X POST "$BASE/ai-support/stream" \
  -H 'Content-Type: application/json' \
  -d '{"message":"Shop có bán cát vệ sinh không?"}' | head -c 600; echo

say "3. MCP tools/list"
curl -s -X POST "$BASE/mcp" -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | head -c 400; echo

say "4. MCP tools/call searchProducts"
curl -s -X POST "$BASE/mcp" -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"searchProducts","arguments":{"query":"pate"}}}' | head -c 600; echo

say "5. merchant digest (admin session required)"
curl -s -c "$JAR" -b "$JAR" "$BASE/admin/ai-merchant/digest" | head -c 600; echo

say "done."
