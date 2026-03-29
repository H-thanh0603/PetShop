<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Siêu Thị Thú Cưng - PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #00bfa5;
            --primary-dark: #008f7a;
            --primary-soft: #e8fbf7;
            --primary-soft-2: #f4fdfb;
            --accent: #ff7a45;
            --ink: #122033;
            --ink-soft: #1a2e5a;
            --text-secondary: #60707d;
            --text-muted: #90a2af;
            --surface: #f5f8f9;
            --white: #ffffff;
            --border: rgba(18, 32, 51, 0.08);
            --shadow-soft: 0 18px 40px rgba(18, 32, 51, 0.08);
            --shadow-green: 0 18px 36px rgba(0, 143, 122, 0.12);
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: 'Be Vietnam Pro', sans-serif;
            background: linear-gradient(180deg, #f8fbfb 0%, #f3f7f8 100%);
            color: var(--ink);
        }
        h1,h2,h3,h4,h5,h6 { font-family: 'Plus Jakarta Sans', sans-serif; }
        .section-pad { padding: 64px 0; }
        .section-header { margin-bottom: 26px; }
        .section-kicker {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--primary-soft);
            color: var(--primary-dark);
            font-size: 0.82rem;
            font-weight: 700;
            margin-bottom: 14px;
        }
        .section-header h2 {
            margin: 0 0 8px;
            font-size: 2rem;
            font-weight: 800;
            letter-spacing: -0.03em;
            color: var(--ink);
        }
        .section-header p {
            margin: 0;
            max-width: 620px;
            line-height: 1.75;
            color: var(--text-secondary);
        }
        .section-link {
            text-decoration: none;
            color: var(--primary-dark);
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .section-link:hover { color: var(--primary-dark); text-decoration: underline; }

        .shop-hero {
            position: relative;
            overflow: hidden;
            padding: 90px 0 72px;
            background:
                radial-gradient(circle at top right, rgba(255,255,255,0.18) 0%, transparent 28%),
                linear-gradient(135deg, #0a3447 0%, #0a756c 52%, var(--primary) 100%);
            color: #fff;
        }
        .shop-hero::before {
            content: "";
            position: absolute;
            width: 520px;
            height: 520px;
            border-radius: 50%;
            background: rgba(255,255,255,0.05);
            top: -210px;
            right: -120px;
        }
        .hero-grid {
            position: relative;
            z-index: 1;
            display: grid;
            grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
            gap: 26px;
            align-items: stretch;
        }
        .hero-copy h1 {
            margin: 0 0 16px;
            font-size: clamp(2.8rem, 5vw, 4.5rem);
            font-weight: 800;
            line-height: 1.04;
            letter-spacing: -0.04em;
        }
        .hero-copy h1 .highlight { color: #dcfff9; }
        .hero-copy p {
            max-width: 620px;
            margin: 0 0 26px;
            color: rgba(255,255,255,0.86);
            line-height: 1.8;
            font-size: 1.03rem;
        }
        .hero-actions, .hero-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }
        .hero-actions { margin-bottom: 22px; }
        .hero-btn,
        .hero-btn-outline {
            padding: 14px 22px;
            border-radius: 999px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-weight: 700;
            transition: all 0.25s ease;
        }
        .hero-btn {
            background: #fff;
            color: var(--primary-dark);
            box-shadow: 0 16px 34px rgba(0,0,0,0.10);
        }
        .hero-btn:hover { color: var(--primary-dark); transform: translateY(-2px); }
        .hero-btn-outline {
            color: #fff;
            border: 1px solid rgba(255,255,255,0.26);
            background: rgba(255,255,255,0.08);
        }
        .hero-btn-outline:hover { color: #fff; background: rgba(255,255,255,0.14); }
        .hero-tag {
            padding: 10px 14px;
            border-radius: 16px;
            background: rgba(255,255,255,0.10);
            color: rgba(255,255,255,0.92);
            border: 1px solid rgba(255,255,255,0.10);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.9rem;
        }
        .hero-side {
            background: rgba(255,255,255,0.10);
            border: 1px solid rgba(255,255,255,0.14);
            border-radius: 28px;
            padding: 24px;
            backdrop-filter: blur(14px);
            box-shadow: 0 20px 44px rgba(0,0,0,0.10);
        }
        .hero-side h3 {
            font-size: 1.2rem;
            font-weight: 800;
            margin-bottom: 8px;
        }
        .hero-side p {
            margin: 0 0 18px;
            color: rgba(255,255,255,0.82);
            line-height: 1.75;
        }
        .hero-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 12px;
            margin-bottom: 18px;
        }
        .hero-stat {
            border-radius: 20px;
            padding: 16px 12px;
            text-align: center;
            background: rgba(255,255,255,0.12);
        }
        .hero-stat strong {
            display: block;
            font-size: 1.5rem;
            font-weight: 800;
            margin-bottom: 4px;
        }
        .hero-stat span {
            font-size: 0.8rem;
            color: rgba(255,255,255,0.82);
        }
        .hero-pills {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }
        .hero-pill {
            padding: 9px 13px;
            border-radius: 999px;
            text-decoration: none;
            color: #fff;
            background: rgba(255,255,255,0.10);
            border: 1px solid rgba(255,255,255,0.10);
            font-size: 0.86rem;
        }
        .hero-pill:hover { color: #fff; background: rgba(255,255,255,0.16); }

        .trust-wrap {
            margin-top: -28px;
            position: relative;
            z-index: 2;
        }
        .trust-grid {
            background: rgba(255,255,255,0.96);
            border-radius: 28px;
            box-shadow: var(--shadow-soft);
            padding: 18px;
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 14px;
        }
        .trust-card {
            display: flex;
            align-items: center;
            gap: 14px;
            padding: 14px 16px;
            border-radius: 20px;
            background: var(--primary-soft-2);
        }
        .trust-icon {
            width: 48px;
            height: 48px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.35rem;
            color: #fff;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            flex-shrink: 0;
        }
        .trust-card strong {
            display: block;
            font-size: 0.95rem;
            margin-bottom: 2px;
        }
        .trust-card span {
            color: var(--text-secondary);
            font-size: 0.82rem;
        }

        .pet-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 18px;
        }
        .pet-card {
            background: var(--white);
            border-radius: 26px;
            padding: 22px;
            display: flex;
            align-items: center;
            gap: 16px;
            text-decoration: none;
            color: var(--ink);
            border: 1px solid rgba(0, 191, 165, 0.10);
            box-shadow: var(--shadow-green);
            transition: all 0.25s ease;
        }
        .pet-card:hover { color: var(--ink); transform: translateY(-4px); }
        .pet-icon {
            width: 62px;
            height: 62px;
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, var(--primary-soft), #ffffff);
            color: var(--primary-dark);
            font-size: 1.9rem;
            flex-shrink: 0;
        }
        .pet-card strong {
            display: block;
            margin-bottom: 4px;
            font-size: 1rem;
        }
        .pet-card span {
            color: var(--text-secondary);
            font-size: 0.88rem;
        }

        .category-shell {
            background: linear-gradient(180deg, #ffffff 0%, #fbfefe 100%);
            border-radius: 32px;
            padding: 28px;
            border: 1px solid rgba(0, 191, 165, 0.10);
            box-shadow: var(--shadow-soft);
        }
        .category-track {
            display: flex;
            gap: 24px;
            overflow-x: auto;
            scroll-snap-type: x mandatory;
            padding-bottom: 12px;
            scrollbar-width: none;
        }
        .category-track::-webkit-scrollbar { display: none; }
        .category-card {
            min-width: 166px;
            max-width: 166px;
            text-decoration: none;
            color: var(--ink-soft);
            text-align: center;
            scroll-snap-align: start;
        }
        .category-card:hover { color: var(--ink-soft); }
        .category-icon {
            width: 150px;
            height: 150px;
            margin: 0 auto 18px;
            border-radius: 50%;
            border: 6px solid #fff;
            background: radial-gradient(circle at 28% 28%, #17d8c0 0%, var(--primary) 40%, var(--primary-dark) 74%, #0c6c63 100%);
            box-shadow: var(--shadow-green);
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.25s ease;
        }
        .category-card:hover .category-icon {
            transform: translateY(-4px);
            box-shadow: 0 22px 42px rgba(0, 143, 122, 0.18);
        }
        .category-icon i {
            color: #fff;
            font-size: 3.9rem;
        }
        .category-card strong {
            font-size: 1rem;
            font-weight: 600;
            line-height: 1.45;
            min-height: 48px;
            display: block;
        }

        .filter-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 18px;
        }
        .filter-card {
            background: var(--white);
            border-radius: 24px;
            padding: 24px;
            box-shadow: var(--shadow-soft);
            border: 1px solid var(--border);
            text-decoration: none;
            color: var(--ink);
            transition: all 0.25s ease;
        }
        .filter-card:hover { color: var(--ink); transform: translateY(-4px); }
        .filter-card .icon {
            width: 52px;
            height: 52px;
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
            margin-bottom: 16px;
        }
        .filter-card strong {
            display: block;
            font-size: 1rem;
            margin-bottom: 8px;
        }
        .filter-card p {
            margin: 0;
            color: var(--text-secondary);
            line-height: 1.65;
            font-size: 0.88rem;
        }

        .product-layout {
            display: grid;
            grid-template-columns: minmax(320px, 0.95fr) minmax(0, 1.05fr);
            gap: 22px;
        }
        .spotlight {
            background: linear-gradient(145deg, #e9fbf7 0%, #ffffff 100%);
            border-radius: 32px;
            padding: 28px;
            box-shadow: var(--shadow-green);
            border: 1px solid rgba(0, 191, 165, 0.12);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }
        .spotlight-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 12px;
            border-radius: 999px;
            background: rgba(255,255,255,0.88);
            color: var(--primary-dark);
            font-size: 0.8rem;
            font-weight: 700;
            margin-bottom: 16px;
        }
        .spotlight h3 {
            margin: 0 0 12px;
            font-size: 1.7rem;
            font-weight: 800;
            line-height: 1.2;
        }
        .spotlight p {
            margin: 0 0 16px;
            color: var(--text-secondary);
            line-height: 1.75;
        }
        .spotlight-price {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 10px;
            margin-bottom: 18px;
        }
        .spotlight-price .new {
            font-size: 1.5rem;
            font-weight: 800;
            color: var(--primary-dark);
        }
        .spotlight-price .old {
            color: var(--text-muted);
            text-decoration: line-through;
        }
        .spotlight-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin-bottom: 22px;
        }
        .btn-soft, .btn-dark {
            padding: 12px 18px;
            border-radius: 999px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-weight: 700;
            border: none;
        }
        .btn-soft {
            background: #fff;
            color: var(--primary-dark);
            box-shadow: 0 10px 24px rgba(18, 32, 51, 0.08);
        }
        .btn-soft:hover { color: var(--primary-dark); }
        .btn-dark {
            background: var(--ink-soft);
            color: #fff;
        }
        .btn-dark:hover { color: #fff; }
        .spotlight-image {
            min-height: 220px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .spotlight-image img {
            max-width: 100%;
            max-height: 220px;
            object-fit: contain;
            filter: drop-shadow(0 20px 28px rgba(0,0,0,0.10));
        }

        .product-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }
        .product-card {
            background: var(--white);
            border-radius: 26px;
            overflow: hidden;
            box-shadow: var(--shadow-soft);
            border: 1px solid rgba(18, 32, 51, 0.06);
            transition: all 0.25s ease;
        }
        .product-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 18px 40px rgba(18, 32, 51, 0.10);
        }
        .product-media {
            min-height: 210px;
            background: linear-gradient(180deg, #f9fcfc 0%, #eef6f6 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            position: relative;
        }
        .product-media img {
            max-width: 100%;
            max-height: 170px;
            object-fit: contain;
            transition: transform 0.3s ease;
        }
        .product-card:hover .product-media img { transform: scale(1.04); }
        .product-badge {
            position: absolute;
            top: 16px;
            left: 16px;
            padding: 7px 12px;
            border-radius: 999px;
            background: var(--accent);
            color: #fff;
            font-size: 0.75rem;
            font-weight: 800;
        }
        .product-info { padding: 18px; }
        .product-cat {
            margin-bottom: 8px;
            font-size: 0.72rem;
            font-weight: 800;
            color: var(--primary-dark);
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }
        .product-name {
            margin-bottom: 14px;
            min-height: 46px;
            line-height: 1.45;
            font-size: 0.96rem;
            font-weight: 700;
        }
        .product-name a { color: var(--ink); text-decoration: none; }
        .product-name a:hover { color: var(--primary-dark); }
        .product-meta {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
        }
        .price-wrap strong {
            display: block;
            font-size: 1.04rem;
            font-weight: 800;
        }
        .price-wrap span {
            display: block;
            margin-top: 2px;
            color: var(--text-muted);
            text-decoration: line-through;
            font-size: 0.82rem;
        }
        .cart-btn {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            border: none;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 1.15rem;
        }

        .sale-shell {
            display: grid;
            grid-template-columns: 1.05fr 0.95fr;
            gap: 20px;
        }
        .sale-banner {
            position: relative;
            overflow: hidden;
            border-radius: 32px;
            padding: 30px;
            background: linear-gradient(145deg, #112848 0%, #1a2e5a 52%, #00bfa5 100%);
            color: #fff;
            box-shadow: 0 24px 48px rgba(17, 40, 72, 0.18);
        }
        .sale-banner::after {
            content: "";
            position: absolute;
            width: 280px;
            height: 280px;
            right: -100px;
            bottom: -120px;
            border-radius: 50%;
            background: rgba(255,255,255,0.06);
        }
        .sale-banner h3 {
            position: relative;
            z-index: 1;
            margin: 0 0 12px;
            font-size: 2rem;
            font-weight: 800;
            line-height: 1.15;
        }
        .sale-banner p {
            position: relative;
            z-index: 1;
            margin: 0 0 18px;
            color: rgba(255,255,255,0.84);
            line-height: 1.75;
        }
        .sale-tags {
            position: relative;
            z-index: 1;
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 18px;
        }
        .sale-tags span {
            padding: 10px 14px;
            border-radius: 999px;
            background: rgba(255,255,255,0.10);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.88rem;
        }
        .sale-mini-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }
        .sale-mini-card {
            background: var(--white);
            border-radius: 24px;
            padding: 16px;
            box-shadow: var(--shadow-soft);
        }
        .sale-mini-thumb {
            height: 146px;
            border-radius: 18px;
            background: var(--surface);
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            margin-bottom: 14px;
            overflow: hidden;
        }
        .sale-mini-thumb img {
            max-width: 100%;
            max-height: 118px;
            object-fit: contain;
        }
        .sale-off {
            position: absolute;
            top: 10px;
            left: 10px;
            padding: 6px 10px;
            border-radius: 999px;
            background: var(--accent);
            color: #fff;
            font-size: 0.75rem;
            font-weight: 800;
        }
        .sale-mini-card h6 {
            margin: 0 0 10px;
            min-height: 42px;
            font-size: 0.94rem;
            line-height: 1.45;
            font-weight: 700;
        }
        .sale-mini-card h6 a { color: var(--ink); text-decoration: none; }
        .sale-mini-card h6 a:hover { color: var(--primary-dark); }
        .sale-mini-card strong { color: var(--primary-dark); }
        .sale-mini-card span {
            margin-left: 8px;
            color: var(--text-muted);
            text-decoration: line-through;
            font-size: 0.82rem;
        }

        .experience-grid {
            display: grid;
            grid-template-columns: 1.05fr 0.95fr;
            gap: 20px;
        }
        .service-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }
        .service-card {
            background: var(--white);
            border-radius: 24px;
            padding: 24px;
            box-shadow: var(--shadow-soft);
            border: 1px solid var(--border);
        }
        .service-card .icon {
            width: 54px;
            height: 54px;
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.45rem;
            margin-bottom: 16px;
        }
        .service-card h5 {
            margin: 0 0 8px;
            font-size: 1rem;
            font-weight: 800;
        }
        .service-card p {
            margin: 0;
            color: var(--text-secondary);
            line-height: 1.7;
            font-size: 0.9rem;
        }
        .guide-card {
            background: linear-gradient(180deg, #ffffff 0%, #f8fefd 100%);
            border-radius: 30px;
            padding: 28px;
            border: 1px solid rgba(0, 191, 165, 0.10);
            box-shadow: var(--shadow-soft);
        }
        .guide-list {
            display: grid;
            gap: 16px;
            margin-top: 18px;
        }
        .guide-item {
            display: flex;
            gap: 14px;
            align-items: flex-start;
        }
        .guide-step {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-weight: 800;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            flex-shrink: 0;
        }
        .guide-item strong {
            display: block;
            margin-bottom: 4px;
            font-size: 0.96rem;
        }
        .guide-item p {
            margin: 0;
            color: var(--text-secondary);
            line-height: 1.65;
            font-size: 0.9rem;
        }
        .guide-cta {
            margin-top: 22px;
            padding: 18px 20px;
            border-radius: 20px;
            background: var(--primary-soft);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 14px;
            flex-wrap: wrap;
        }
        .guide-cta strong {
            display: block;
            margin-bottom: 3px;
            font-size: 1rem;
        }
        .guide-cta span {
            color: var(--text-secondary);
            font-size: 0.88rem;
        }

        @media (max-width: 1199px) {
            .hero-grid,
            .product-layout,
            .sale-shell,
            .experience-grid { grid-template-columns: 1fr; }
            .trust-grid,
            .filter-grid { grid-template-columns: repeat(2, 1fr); }
        }
        @media (max-width: 991px) {
            .service-grid,
            .sale-mini-grid,
            .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .hero-stats { grid-template-columns: repeat(3, 1fr); }
        }
        @media (max-width: 767px) {
            .section-pad { padding: 50px 0; }
            .shop-hero { padding: 78px 0 58px; }
            .hero-copy h1 { font-size: 2.5rem; }
            .hero-actions, .hero-tags { flex-direction: column; align-items: stretch; }
            .trust-grid,
            .filter-grid,
            .product-grid,
            .sale-mini-grid,
            .service-grid,
            .hero-stats { grid-template-columns: 1fr; }
            .category-card { min-width: 142px; max-width: 142px; }
            .category-icon {
                width: 126px;
                height: 126px;
                margin-bottom: 16px;
            }
            .category-icon i { font-size: 3rem; }
        }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <section class="shop-hero">
        <div class="container">
            <div class="hero-grid">
                <div class="hero-copy">
                    <span class="section-kicker"><i class='bx bxs-store'></i> PetShop Marketplace</span>
                    <h1>Mua sắm cho thú cưng theo cách <span class="highlight">đẹp hơn, nhanh hơn và rõ ràng hơn</span></h1>
                    <p>Trang shop được làm lại theo kiểu storefront hiện đại: có hero nổi bật, điều hướng nhanh, danh mục trực quan, sản phẩm spotlight, khu deal riêng và các khối tăng niềm tin khi mua hàng.</p>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="hero-btn"><i class='bx bxs-hot'></i> Xem ưu đãi hôm nay</a>
                        <a href="${pageContext.request.contextPath}/shop?sort=name" class="hero-btn-outline"><i class='bx bx-grid-alt'></i> Duyệt toàn bộ sản phẩm</a>
                    </div>
                    <div class="hero-tags">
                        <span class="hero-tag"><i class='bx bx-badge-check'></i> Hàng chính hãng</span>
                        <span class="hero-tag"><i class='bx bx-timer'></i> Giao nhanh trong ngày</span>
                        <span class="hero-tag"><i class='bx bx-support'></i> Hỗ trợ 24/7</span>
                    </div>
                </div>
                <div class="hero-side">
                    <h3>Đi nhanh đến đúng khu mua sắm</h3>
                    <p>Khách hàng có thể chọn theo thú cưng, mở khu giảm giá, hoặc đi thẳng tới nhóm danh mục được mua nhiều nhất ngay ở đầu trang.</p>
                    <div class="hero-stats">
                        <div class="hero-stat"><strong>${totalProducts}</strong><span>Sản phẩm</span></div>
                        <div class="hero-stat"><strong>${not empty categories ? categories.size() : 0}</strong><span>Danh mục</span></div>
                        <div class="hero-stat"><strong>${not empty petTypes ? petTypes.size() : 0}</strong><span>Nhóm pet</span></div>
                    </div>
                    <div class="hero-pills">
                        <c:choose>
                            <c:when test="${not empty petTypes}">
                                <c:forEach var="pt" items="${petTypes}" varStatus="st">
                                    <c:if test="${st.index < 4}">
                                        <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" class="hero-pill"><i class='bx ${pt.icon}'></i> ${pt.name}</a>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/shop?pet=dog" class="hero-pill"><i class='bx bxs-dog'></i> Chó</a>
                                <a href="${pageContext.request.contextPath}/shop?pet=cat" class="hero-pill"><i class='bx bxs-cat'></i> Mèo</a>
                            </c:otherwise>
                        </c:choose>
                        <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="hero-pill"><i class='bx bxs-discount'></i> Sale</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="trust-wrap">
        <div class="container">
            <div class="trust-grid">
                <div class="trust-card">
                    <div class="trust-icon"><i class='bx bx-package'></i></div>
                    <div><strong>Đóng gói cẩn thận</strong><span>Giữ hàng sạch đẹp khi giao tới tay khách</span></div>
                </div>
                <div class="trust-card">
                    <div class="trust-icon"><i class='bx bx-shield-quarter'></i></div>
                    <div><strong>Nguồn gốc rõ ràng</strong><span>Ưu tiên sản phẩm chính hãng và uy tín</span></div>
                </div>
                <div class="trust-card">
                    <div class="trust-icon"><i class='bx bx-refresh'></i></div>
                    <div><strong>Hỗ trợ đổi trả</strong><span>Linh hoạt khi phát sinh lỗi sản phẩm</span></div>
                </div>
                <div class="trust-card">
                    <div class="trust-icon"><i class='bx bx-chat'></i></div>
                    <div><strong>Tư vấn trước khi mua</strong><span>Giúp chọn đúng nhu cầu của thú cưng</span></div>
                </div>
            </div>
        </div>
    </section>

    <section class="section-pad" style="padding-bottom: 24px;">
        <div class="container">
            <div class="section-header">
                <span class="section-kicker"><i class='bx bx-paw'></i> Shop theo bé thú cưng</span>
                <h2>Chọn nhanh khu mua sắm phù hợp</h2>
                <p>Đây là lớp điều hướng đầu tiên để khách hàng thu hẹp phạm vi sản phẩm chỉ sau một lần chạm.</p>
            </div>
            <div class="pet-grid">
                <c:choose>
                    <c:when test="${not empty petTypes}">
                        <c:forEach var="pt" items="${petTypes}">
                            <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" class="pet-card">
                                <div class="pet-icon"><i class='bx ${pt.icon}'></i></div>
                                <div>
                                    <strong>Dành cho ${pt.name}</strong>
                                    <span>Danh mục được nhóm sẵn để dễ mua đúng hơn.</span>
                                </div>
                            </a>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/shop?pet=dog" class="pet-card">
                            <div class="pet-icon"><i class='bx bxs-dog'></i></div>
                            <div><strong>Dành cho Chó</strong><span>Từ thức ăn tới phụ kiện, vệ sinh và sức khỏe.</span></div>
                        </a>
                        <a href="${pageContext.request.contextPath}/shop?pet=cat" class="pet-card">
                            <div class="pet-icon"><i class='bx bxs-cat'></i></div>
                            <div><strong>Dành cho Mèo</strong><span>Đi nhanh vào khu ăn uống, cát mèo và phụ kiện.</span></div>
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <section class="section-pad">
        <div class="container">
            <div class="section-header d-flex flex-wrap justify-content-between align-items-end gap-3">
                <div>
                    <span class="section-kicker"><i class='bx bx-category-alt'></i> Danh mục nổi bật</span>
                    <h2>Danh Mục Được Mua Nhiều</h2>
                    <p>Thiết kế lại theo kiểu icon tròn lớn, rõ và đồng bộ với màu thương hiệu PetShop để phần này trông hiện đại hơn kiểu thẻ danh mục cũ.</p>
                </div>
                <a href="${pageContext.request.contextPath}/shop?sort=name" class="section-link">Xem toàn bộ danh mục <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="category-shell">
                <div class="category-track">
                    <c:forEach var="cat" items="${categories}" varStatus="st">
                        <c:if test="${st.index < 10}">
                            <a href="${pageContext.request.contextPath}/shop?category=${cat}" class="category-card">
                                <div class="category-icon">
                                    <c:choose>
                                        <c:when test="${cat.contains('Thức Ăn')}"><i class='bx bx-bowl-hot'></i></c:when>
                                        <c:when test="${cat.contains('Sữa')}"><i class='bx bx-coffee-togo'></i></c:when>
                                        <c:when test="${cat.contains('Vệ Sinh') || cat.contains('Tắm')}"><i class='bx bx-spray-can'></i></c:when>
                                        <c:when test="${cat.contains('Cát')}"><i class='bx bx-archive'></i></c:when>
                                        <c:when test="${cat.contains('Ăn Uống')}"><i class='bx bx-dish'></i></c:when>
                                        <c:when test="${cat.contains('Sức Khoẻ')}"><i class='bx bx-plus-medical'></i></c:when>
                                        <c:when test="${cat.contains('Huấn Luyện') || cat.contains('Đồ Chơi')}"><i class='bx bx-bone'></i></c:when>
                                        <c:when test="${cat.contains('Dụng Cụ Vệ Sinh')}"><i class='bx bx-brush'></i></c:when>
                                        <c:otherwise><i class='bx bx-paw'></i></c:otherwise>
                                    </c:choose>
                                </div>
                                <strong>${cat}</strong>
                            </a>
                        </c:if>
                    </c:forEach>
                </div>
            </div>
        </div>
    </section>

    <section class="section-pad" style="padding-top: 0;">
        <div class="container">
            <div class="section-header">
                <span class="section-kicker"><i class='bx bx-slider-alt'></i> Bộ lọc mua sắm nhanh</span>
                <h2>Khối điều hướng giống một trang shop hiện nay</h2>
                <p>Thay vì bắt đầu từ một lưới sản phẩm dài, người dùng được đi qua các lối tắt phổ biến để chạm vào đúng thứ họ cần nhanh hơn.</p>
            </div>
            <div class="filter-grid">
                <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="filter-card">
                    <div class="icon" style="background:#fff0e7;color:#ff7a45;"><i class='bx bxs-discount'></i></div>
                    <strong>Ưu đãi trong ngày</strong>
                    <p>Mở nhanh khu deal để tối ưu ngân sách trước khi duyệt sâu hơn.</p>
                </a>
                <a href="${pageContext.request.contextPath}/shop?sort=price-asc" class="filter-card">
                    <div class="icon" style="background:var(--primary-soft);color:var(--primary-dark);"><i class='bx bx-sort-up'></i></div>
                    <strong>Giá từ thấp đến cao</strong>
                    <p>Phù hợp khi khách muốn lọc theo ngân sách trước tiên.</p>
                </a>
                <a href="${pageContext.request.contextPath}/shop?sort=discount" class="filter-card">
                    <div class="icon" style="background:#eef4ff;color:#3267ff;"><i class='bx bx-trending-up'></i></div>
                    <strong>Deal mạnh nhất</strong>
                    <p>Ưu tiên những sản phẩm có mức giảm tốt để dễ quyết định mua.</p>
                </a>
                <a href="${pageContext.request.contextPath}/shop?sort=name" class="filter-card">
                    <div class="icon" style="background:#fff7ea;color:#d97706;"><i class='bx bx-list-ul'></i></div>
                    <strong>Duyệt toàn bộ</strong>
                    <p>Hiển thị danh sách đầy đủ để khách khám phá thêm sản phẩm mới.</p>
                </a>
            </div>
        </div>
    </section>

    <section class="section-pad">
        <div class="container">
            <div class="section-header d-flex flex-wrap justify-content-between align-items-end gap-3">
                <div>
                    <span class="section-kicker"><i class='bx bx-star'></i> Gợi ý sản phẩm</span>
                    <h2>Spotlight + lưới sản phẩm hiện đại</h2>
                    <p>Phần này giúp trang shop có nhịp điệu tốt hơn: một sản phẩm được nhấn mạnh, bên cạnh là lưới thẻ mua nhanh như các storefront hiện đại.</p>
                </div>
                <a href="${pageContext.request.contextPath}/shop?sort=name" class="section-link">Mở toàn bộ sản phẩm <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <c:choose>
                <c:when test="${not empty products}">
                    <div class="product-layout">
                        <div class="spotlight">
                            <div>
                                <span class="spotlight-badge"><i class='bx bxs-hot'></i> Sản phẩm nổi bật</span>
                                <h3>${products[0].name}</h3>
                                <p>${not empty products[0].description ? products[0].description : 'Lựa chọn đáng chú ý cho nhu cầu chăm sóc hàng ngày của thú cưng.'}</p>
                                <div class="spotlight-price">
                                    <span class="new">${products[0].formattedPrice}</span>
                                    <c:if test="${products[0].discount > 0}">
                                        <span class="old">${products[0].formattedOldPrice}</span>
                                    </c:if>
                                </div>
                                <div class="spotlight-actions">
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${products[0].id}" class="btn-soft"><i class='bx bx-show-alt'></i> Xem chi tiết</a>
                                    <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                        <input type="hidden" name="productId" value="${products[0].id}">
                                        <input type="hidden" name="quantity" value="1">
                                        <button type="submit" class="btn-dark"><i class='bx bx-cart-add'></i> Thêm vào giỏ</button>
                                    </form>
                                </div>
                            </div>
                            <div class="spotlight-image">
                                <c:choose>
                                    <c:when test="${products[0].image.startsWith('http')}">
                                        <img src="${products[0].image}" alt="${products[0].name}" onerror="this.src='https://placehold.co/280x280/e6fbf7/008f7a?text=PetShop'">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/${products[0].image}" alt="${products[0].name}" onerror="this.src='https://placehold.co/280x280/e6fbf7/008f7a?text=PetShop'">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="product-grid">
                            <c:forEach var="p" items="${products}" varStatus="st">
                                <c:if test="${st.index > 0 && st.index < 7}">
                                    <div class="product-card">
                                        <div class="product-media">
                                            <c:if test="${p.discount > 0}">
                                                <span class="product-badge">-${p.discount}%</span>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                                <c:choose>
                                                    <c:when test="${p.image.startsWith('http')}">
                                                        <img src="${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/220x220/e6fbf7/008f7a?text=PetShop'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/220x220/e6fbf7/008f7a?text=PetShop'">
                                                    </c:otherwise>
                                                </c:choose>
                                            </a>
                                        </div>
                                        <div class="product-info">
                                            <div class="product-cat">${p.category}</div>
                                            <div class="product-name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                            <div class="product-meta">
                                                <div class="price-wrap">
                                                    <strong>${p.formattedPrice}</strong>
                                                    <c:if test="${p.discount > 0}">
                                                        <span>${p.formattedOldPrice}</span>
                                                    </c:if>
                                                </div>
                                                <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                                    <input type="hidden" name="productId" value="${p.id}">
                                                    <input type="hidden" name="quantity" value="1">
                                                    <button type="submit" class="cart-btn" title="Thêm vào giỏ"><i class='bx bx-cart-add'></i></button>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <i class='bx bx-package' style="font-size:4rem;color:var(--text-muted);"></i>
                        <p class="mt-3 mb-0" style="color:var(--text-secondary);">Hiện chưa có sản phẩm để hiển thị.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <c:if test="${not empty discountProducts}">
        <section class="section-pad" style="padding-top: 0;">
            <div class="container">
                <div class="section-header d-flex flex-wrap justify-content-between align-items-end gap-3">
                    <div>
                        <span class="section-kicker"><i class='bx bxs-discount'></i> Khu khuyến mãi</span>
                        <h2>Deal riêng để khách không bỏ lỡ ưu đãi</h2>
                        <p>Khối sale được tách hẳn thành một khu có spotlight và thẻ sản phẩm mini, đúng mô hình mà nhiều trang shop hiện đại đang dùng.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="section-link">Xem tất cả ưu đãi <i class='bx bx-right-arrow-alt'></i></a>
                </div>
                <div class="sale-shell">
                    <div class="sale-banner">
                        <h3>Ưu đãi tốt cho lần mua sắm tiếp theo của bé cưng</h3>
                        <p>Tập trung vào các sản phẩm đang giảm để khách có một điểm vào rõ ràng khi muốn săn deal hoặc mua combo tiết kiệm.</p>
                        <div class="sale-tags">
                            <span><i class='bx bx-check-circle'></i> Giá đang tốt</span>
                            <span><i class='bx bx-check-circle'></i> Dễ gom đơn</span>
                            <span><i class='bx bx-check-circle'></i> Phù hợp mua lặp lại</span>
                        </div>
                        <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="hero-btn"><i class='bx bx-right-arrow-alt'></i> Vào khu giảm giá</a>
                    </div>
                    <div class="sale-mini-grid">
                        <c:forEach var="p" items="${discountProducts}" varStatus="st">
                            <c:if test="${st.index < 4}">
                                <div class="sale-mini-card">
                                    <div class="sale-mini-thumb">
                                        <span class="sale-off">-${p.discount}%</span>
                                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                            <c:choose>
                                                <c:when test="${p.image.startsWith('http')}">
                                                    <img src="${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/180x180/e6fbf7/008f7a?text=Sale'">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/180x180/e6fbf7/008f7a?text=Sale'">
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>
                                    <h6><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></h6>
                                    <div><strong>${p.formattedPrice}</strong><span>${p.formattedOldPrice}</span></div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </section>
    </c:if>

    <section class="section-pad">
        <div class="container">
            <div class="section-header">
                <span class="section-kicker"><i class='bx bx-store-alt'></i> Trải nghiệm mua sắm</span>
                <h2>Đủ các khối quan trọng của một trang shop hiện đại</h2>
                <p>Từ khối niềm tin, danh mục nổi bật, sản phẩm spotlight, khu sale đến phần hướng dẫn chọn nhanh, trang shop giờ đã đầy đặn và dễ dùng hơn nhiều.</p>
            </div>
            <div class="experience-grid">
                <div class="service-grid">
                    <div class="service-card">
                        <div class="icon" style="background:var(--primary-soft);color:var(--primary-dark);"><i class='bx bx-package'></i></div>
                        <h5>Giao hàng nhanh và có kế hoạch</h5>
                        <p>Phù hợp cho những đơn cần gấp như thức ăn, cát mèo hay các vật dụng chăm sóc thường xuyên.</p>
                    </div>
                    <div class="service-card">
                        <div class="icon" style="background:#fff1e8;color:#ff7a45;"><i class='bx bx-shield-quarter'></i></div>
                        <h5>Nguồn gốc sản phẩm rõ ràng</h5>
                        <p>Giúp người mua có niềm tin tốt hơn thay vì chỉ thấy danh sách sản phẩm đơn lẻ.</p>
                    </div>
                    <div class="service-card">
                        <div class="icon" style="background:#eef4ff;color:#3267ff;"><i class='bx bx-refresh'></i></div>
                        <h5>Hỗ trợ sau mua</h5>
                        <p>Tạo cảm giác an tâm hơn nhờ có chỗ đứng rõ ràng cho thông điệp đổi trả và hỗ trợ.</p>
                    </div>
                    <div class="service-card">
                        <div class="icon" style="background:#edfdf4;color:#16a34a;"><i class='bx bx-message-rounded-detail'></i></div>
                        <h5>Tư vấn đúng nhu cầu</h5>
                        <p>Khi khách chưa biết chọn gì, trang vẫn cung cấp lối đi tiếp theo thay vì để họ rời đi.</p>
                    </div>
                </div>
                <div class="guide-card">
                    <span class="section-kicker"><i class='bx bx-bulb'></i> Hướng dẫn chọn nhanh</span>
                    <h3 style="margin:0 0 6px;font-size:1.45rem;font-weight:800;">Mua đúng món nhanh hơn</h3>
                    <p style="margin:0;color:var(--text-secondary);line-height:1.75;">Khối hướng dẫn này giúp trang shop trông chuyên nghiệp hơn và cũng hỗ trợ người dùng mới biết cách bắt đầu mua hàng.</p>
                    <div class="guide-list">
                        <div class="guide-item">
                            <div class="guide-step">1</div>
                            <div><strong>Chọn theo thú cưng</strong><p>Đi vào khu chó, mèo hoặc nhóm pet tương ứng trước khi xem chi tiết sản phẩm.</p></div>
                        </div>
                        <div class="guide-item">
                            <div class="guide-step">2</div>
                            <div><strong>Vào danh mục đúng nhu cầu</strong><p>Từ thức ăn, vệ sinh đến sức khỏe, mỗi lối vào đều đã được làm rõ hơn.</p></div>
                        </div>
                        <div class="guide-item">
                            <div class="guide-step">3</div>
                            <div><strong>So sánh sản phẩm và deal</strong><p>Spotlight và khu sale riêng giúp khách ra quyết định dễ hơn khi mua sắm.</p></div>
                        </div>
                    </div>
                    <div class="guide-cta">
                        <div>
                            <strong>Bắt đầu từ đâu?</strong>
                            <span>Nếu bạn muốn mua nhanh, hãy vào thẳng khu deal hoặc khu danh mục nổi bật.</span>
                        </div>
                        <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="btn-dark"><i class='bx bx-right-arrow-alt'></i> Mở khu ưu đãi</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
