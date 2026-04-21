# Cluster 1 Ecommerce Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove non-ecommerce storefront flows from the active UI and keep the codebase focused on product sales while preserving admin reporting/notification files for later ecommerce adaptation.

**Architecture:** Update the active navigation and upload/documentation surfaces so they no longer point to service, community, booking, or appointment features. Keep disabled legacy admin/reporting files on disk for later conversion, but make sure the current storefront and admin entry points are ecommerce-only.

**Tech Stack:** Java Servlet/JSP, Maven, Tomcat, Windows batch.

---

### Task 1: Remove non-ecommerce links from the active storefront navigation

**Files:**
- Modify: `src/main/webapp/components/navbar-white.jsp`

- [ ] **Step 1: Remove service/community/booking links from the public navbar**

Replace the block that links to `/services`, `/community`, `/schedule`, and `/booking` with ecommerce links only.

- [ ] **Step 2: Keep logged-in user dropdown focused on shopping/account**

Remove the “Lịch hẹn của tôi” item so the dropdown only contains ecommerce-relevant actions.

- [ ] **Step 3: Verify there are no remaining references in this file**

Run: `Select-String -Path src\main\webapp\components\navbar-white.jsp -Pattern '/services|/community|/schedule|/booking' -CaseSensitive:$false`

Expected: no matches

### Task 2: Make admin upload and docs ecommerce-only

**Files:**
- Modify: `src/main/java/controller/admin/FileUploadServlet.java`
- Modify: `README.md`

- [ ] **Step 1: Remove blog/community upload language**

Keep product uploads as the primary supported flow and change comments/messages so they no longer describe blog/community usage as active functionality.

- [ ] **Step 2: Add ecommerce-focused project note**

Update `README.md` so it describes the project as a pet-products ecommerce site and mentions the active DB password flow.

- [ ] **Step 3: Verify no active public docs still advertise booking/community**

Run: `Get-ChildItem -Recurse -File README.md,src\main\webapp\components,src\main\java\controller\admin | Select-String -Pattern '/services|/community|/schedule|/booking|blog' -CaseSensitive:$false`

Expected: only legacy disabled files or non-active comments remain

### Task 3: Verify cleanup builds cleanly

**Files:**
- Verify only

- [ ] **Step 1: Compile after cleanup**

Run: `mvn -q -DskipTests compile`

Expected: exit code `0`

- [ ] **Step 2: Inspect working tree**

Run: `git status --short`

Expected: only the intended cleanup files are modified plus existing unrelated untracked local artifacts
