# Cluster 5 E-commerce Admin Insights Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace leftover appointment-oriented admin reporting/notification flows with e-commerce operational insights for orders, inventory, coupons, and product reviews.

**Architecture:** Extend `ReportDAO` as the read model for admin analytics, then reconnect `DashboardServlet`, `ReportServlet`, and `NotificationServlet` to JSP pages that render only e-commerce data. Keep statistics charts intact and make reports/notifications complementary operational views.

**Tech Stack:** Java Servlets, JSP/JSTL, JDBC/MySQL, existing DAO/model layer.

---

## Scope summary
- Dashboard shows sales, low-stock, recent orders, and recent reviews.
- Reports page shows top products, top customers, coupon usage, order status distribution, low-stock products, and low-rating reviews.
- Notifications page becomes an admin alert center for pending orders, low inventory, low-rating reviews, and stored notifications.
