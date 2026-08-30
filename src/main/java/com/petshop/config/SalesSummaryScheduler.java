package com.petshop.config;

import DAO.SalesSummaryDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps the daily_sales_summary rollup fresh: the admin dashboard reads
 * pre-aggregated daily rows instead of scanning the whole orders table.
 *
 * Each run recomputes the last 7 days (covers retroactive status changes);
 * if the table is empty (e.g. data was wiped), a full rebuild is triggered.
 */
@Component
public class SalesSummaryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SalesSummaryScheduler.class);

    private final SalesSummaryDAO salesSummaryDAO = new SalesSummaryDAO();
    private final AtomicBoolean seeded = new AtomicBoolean(false);

    @Scheduled(initialDelay = 120_000, fixedDelay = 600_000)
    public void refresh() {
        try {
            salesSummaryDAO.refreshRecent(7);
            if (seeded.compareAndSet(false, true)) {
                // On the first run after boot, make sure the whole history is
                // present (cheap on an up-to-date table, self-healing if empty).
                salesSummaryDAO.rebuildAll();
            }
        } catch (Exception e) {
            logger.error("Sales summary refresh failed", e);
        }
    }
}
