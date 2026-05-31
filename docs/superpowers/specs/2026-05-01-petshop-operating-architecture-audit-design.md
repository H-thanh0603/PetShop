# PetShop Operating Architecture And Audit Design

**Date:** 2026-05-01

**Status:** Draft for review

## Goal

Create a full operating architecture and audit handbook for PetShop that is practical enough for real deployment thinking, while still being easy to explain in a project defense.

The document should help answer questions like:

- What breaks first if the site grows to around 5,000 users?
- How should more than 1,000 products load quickly?
- What happens if users spam, brute-force login, or abuse checkout?
- What should happen after 30 minutes of inactivity?
- How long should pending payment and transfer verification wait?
- How should the system track newly imported stock, old stock, and near-expiry stock?
- How do we know when and how much inventory to reorder?
- What hidden operational risks exist outside feature-level coding?

## Audience

This handbook is written for three audiences at once:

- the project owner who needs a realistic upgrade roadmap
- the student who needs a strong, structured explanation for defense/demo
- the future maintainer who needs to understand where the current design is fragile

## Non-Goals

This document does **not**:

- implement the solutions directly in code
- introduce production infrastructure immediately
- replace technical specs for individual features
- act as a legal compliance guarantee

It is an operating and risk-design document, not a deployment script or code patch by itself.

## Recommended Positioning

The document should be framed as:

> "PetShop currently works as a functional e-commerce project for demo and controlled use. To move toward a stable real-world deployment, the system must evolve across performance, security, payment operations, inventory operations, observability, and recovery planning."

That positioning is important because it is honest, technically mature, and easy to defend.

## Structure

The handbook should have three layers.

### 1. Executive summary

The opening section should give a quick overview:

- current maturity level of the project
- major operational strengths
- top risks
- what must be done before wider usage
- what can wait until traffic or data scale increases

This section should be readable in 3 to 5 minutes.

### 2. Domain chapters

The main body should be divided into operational domains:

1. system capacity and performance
2. database and query architecture
3. caching and fast product browsing
4. spam, abuse, and brute-force protection
5. authentication, sessions, and idle timeout
6. checkout, payment waiting time, and reconciliation risk
7. inventory, import batches, and stock aging
8. expiry-date handling and near-expiry operations
9. procurement and reorder planning
10. admin governance and auditability
11. observability, logging, and incident response
12. backup, recovery, and rollback
13. security baseline
14. privacy and legal basics

### 3. Action matrix

The closing section should summarize work by priority:

- `P0`: should fix before wider real use
- `P1`: important next-step hardening
- `P2`: useful scale and operations improvements
- `P3`: later optimization and maturity work

## Writing Style

Each domain chapter should be written in a repeatable format:

### Section template

- `What the problem is`
- `Why it matters for PetShop`
- `What can go wrong if ignored`
- `Current project state`
- `Short-term recommendation for this project`
- `Medium-term upgrade path`
- `Priority`

This keeps the document easy to read for both technical and non-technical reviewers.

## Content Scope By Domain

### 1. System capacity and performance

This chapter should explain:

- what "5,000 users" really means: total accounts vs concurrent users
- likely bottlenecks in a servlet/JSP + MySQL architecture
- where the first pain will appear:
  - product list queries
  - image delivery
  - session load
  - DB connection exhaustion
  - checkout contention
- realistic performance targets for this project stage
- signs that the system is reaching its limit

It should clearly distinguish:

- 5,000 registered users
- 5,000 daily active users
- 5,000 concurrent users

because these are very different load profiles and are often confused in defense questions.

### 2. Database and query architecture

This chapter should cover:

- the risk of full table scans as products/orders/users grow
- where indexing matters most:
  - products
  - orders
  - order items
  - users
  - payment transactions
- pagination strategy
- N+1 query risks
- query patterns likely to degrade first
- connection pool sizing concerns
- transaction scope concerns in checkout and stock updates

It should explain the practical meaning of:

- indexes
- filtered search
- pagination
- row locking
- transaction duration

in plain language, tied directly to PetShop pages.

### 3. Caching and fast product browsing

This chapter should answer:

- how to load 1,000+ products without slow pages
- when to cache category lists, pet types, top products, and homepage data
- when not to cache stock-sensitive or checkout-sensitive data
- the role of browser cache, CDN-style asset caching, and application-side caching

The document should recommend a staged approach:

- short-term: DB indexes + pagination + image optimization
- medium-term: cache popular read-heavy storefront data
- later: dedicated cache like Redis if the project grows further

### 4. Spam, abuse, and brute-force protection

This chapter should cover:

- spam review submissions
- spam cart actions
- repeated checkout attempts
- coupon abuse
- brute-force login attempts
- scripted scraping or endpoint hammering

It should discuss controls like:

- rate limiting
- per-IP throttling
- per-account throttling
- CAPTCHA only on suspicious behavior
- lockout windows
- request logging
- abuse alerting

It should also explain the tradeoff between user friction and protection.

### 5. Authentication, sessions, and idle timeout

This chapter should answer:

- what should happen after 30 minutes of inactivity
- whether the session should expire fully or require re-auth only for sensitive actions
- how remember-me differs from active session timeout
- what happens to cart/session state after timeout
- how to protect against session fixation and stolen cookies

It should include a recommended baseline:

- idle session timeout around 30 minutes for admin and standard user sessions
- separate remember-me token policy if present
- forced session regeneration after login
- forced logout after credential-sensitive changes if needed

### 6. Checkout, payment waiting time, and reconciliation risk

This chapter should cover:

- how long a bank-transfer order can remain pending
- when a pending payment should expire
- what happens if a user never transfers
- what happens if the user transfers wrong amount or wrong reference
- what happens if admin verifies late
- what happens if payment is verified after stock has changed

Recommended framing:

- order creation timeout is immediate
- bank-transfer payment verification can wait in a pending state for a defined business window
- the project should define a policy such as 15 minutes, 30 minutes, 2 hours, or 24 hours depending on business model

The document should explain recommended choices and their tradeoffs for this project.

### 7. Inventory, import batches, and stock aging

This chapter should explain how to track:

- products imported today
- products imported 1 day ago
- products imported 1 week ago
- products imported 4 months ago
- stock that has not moved
- stock that sells too quickly

It should recommend a batch-based inventory model, even if the current code does not fully implement it yet:

- each import should create a stock batch
- each batch should record:
  - product
  - quantity
  - import date
  - supplier if available
  - cost price if available
  - expiry date if available
- reports should show stock age and batch age

This is important because inventory questions are common in project defense and are often overlooked in early code.

### 8. Expiry-date handling and near-expiry operations

This chapter should cover:

- how to define "near expiry"
- what to do with products close to expiry
- what to do with expired products
- whether such products should be hidden, discounted, or blocked from sale
- how alerts should work for admin

It should recommend practical states such as:

- healthy stock
- near-expiry stock
- expired stock
- blocked from sale

and explain why expiry logic should be batch-based rather than product-only.

### 9. Procurement and reorder planning

This chapter should answer:

- how to know when to import more stock
- how much to import
- what data to look at
- how to avoid both stockouts and overstock

The document should recommend simple reorder logic first:

- minimum stock threshold
- average weekly sales
- lead time from supplier
- safety stock

Then it should describe how to mature later:

- seasonality awareness
- sales trend analysis
- slow-moving stock detection
- ABC classification

### 10. Admin governance and auditability

This chapter should cover:

- logging admin actions
- order status changes
- payment verification changes
- stock adjustments
- coupon changes
- product data edits

It should explain why audit trails matter:

- accountability
- debugging
- fraud prevention
- conflict resolution

It should also note where the current project already has some audit direction and where it remains incomplete.

### 11. Observability, logging, and incident response

This chapter should explain:

- what logs are needed
- what metrics are useful
- what should trigger alerts
- how to detect incidents before users complain

Recommended areas:

- login failures
- repeated 4xx/5xx responses
- checkout failure rate
- payment verification backlog
- low stock alerts
- DB connection pool stress
- slow query patterns

The goal is not to demand enterprise tooling immediately, but to show a clear monitoring roadmap.

### 12. Backup, recovery, and rollback

This chapter should cover:

- database backup frequency
- file/image backup concerns
- how to restore after accidental deletion or corruption
- how to roll back a bad deployment
- how to test backup validity

It should explain that backup without restore testing is not real backup confidence.

### 13. Security baseline

This chapter should consolidate practical security expectations:

- CSRF protection
- XSS prevention
- SQL injection safety
- secure password storage
- secure cookie/session configuration
- rate limiting
- admin authorization boundaries
- upload restrictions
- audit logging

It should clearly separate:

- what is already partially present
- what still needs hardening before real exposure

### 14. Privacy and legal basics

This chapter should stay lightweight but mature:

- personal data collection scope
- password and contact data handling
- order/address retention concerns
- customer notification expectations
- basic terms/privacy disclosure expectations

It should not overclaim compliance, but should show that the project owner understands the issue.

## Key Operating Questions To Address Explicitly

The handbook must include explicit answers for the following likely defense questions:

### If the number of users grows to 5,000, what happens?

The answer should explain:

- likely still manageable if 5,000 means registered users
- likely strained if concurrent traffic spikes without indexing, caching, image optimization, and pooled DB tuning
- where scale breaks first and how to fix it progressively

### How to load more than 1,000 products quickly?

The answer should include:

- pagination
- filtering
- DB indexes
- optimized images
- lazy loading
- caching non-sensitive read paths

### What if users spam the system?

The answer should include:

- rate limiting
- CAPTCHA for suspicious patterns
- request throttling
- abuse logging
- moderation for high-risk inputs

### What if a user enters the wrong password more than 5 times?

The answer should include:

- temporary lockout
- lock duration
- alert logging
- possible CAPTCHA challenge
- balance between security and user recovery

### What if a user is inactive for 30 minutes?

The answer should include:

- idle session expiration
- cart persistence choice
- re-login requirement for sensitive operations

### How long should payment stay pending?

The answer should compare policies like:

- 15 minutes
- 30 minutes
- 2 hours
- 24 hours

and recommend a realistic value for this project.

### How to track stock imported 1 day ago, 1 week ago, or 4 months ago?

The answer should explain why import batches or stock lots are necessary.

### How to handle near-expiry stock?

The answer should explain:

- batch expiry dates
- warning thresholds
- discount or block rules
- disposal logic

### How to know when to reorder?

The answer should include:

- minimum threshold
- lead time
- sales velocity
- safety stock

## Risk Framing

The document should classify risks by priority.

### P0

Issues that block safe wider use, such as:

- payment/order status inconsistency
- missing abuse protection on login or checkout
- weak admin protection
- no restore plan
- no inventory aging or expiry model if selling perishable goods

### P1

Important hardening after the first stable version:

- better caching
- better monitoring
- batch inventory tracking
- lockout and alerting improvements
- clearer payment expiry policy

### P2

Useful scale improvements:

- richer analytics
- reorder forecasting
- dashboard metrics
- more detailed audit trails

### P3

Later maturity work:

- advanced forecasting
- richer observability stack
- multi-node scaling strategies

## Expected Deliverable

The final document produced from this design should be:

- detailed
- structured
- specific to PetShop
- honest about current limits
- practical enough to become a roadmap
- clear enough to cite directly during project defense

## Implementation Boundary

This design produces one document artifact first:

- a full operating architecture and audit handbook for PetShop

It does not itself implement:

- caching infrastructure
- stock batch schema
- payment expiry jobs
- new monitoring services

Those may become later implementation specs after the handbook is complete.

## Why This Design Fits

This approach is the best fit because:

- it answers the exact real-world questions the user raised
- it is broad enough to surface hidden operational gaps
- it avoids shallow checklist-only advice
- it stays grounded in the current PetShop architecture rather than generic theory

## Open Decisions Resolved

The following decisions are now locked for this document:

- the document should be practical, not purely academic
- it should still be easy to use in a project defense
- it should go deep across performance, security, payment, inventory, observability, and recovery
- it should be written as a structured handbook with priorities and recommendations
