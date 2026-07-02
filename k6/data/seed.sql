-- =============================================================
-- k6 성능 테스트 마스터 데이터 시드
-- =============================================================
-- 실행 방법:
--   mysql -u root -p1234 cosmetic_wms < k6/data/seed.sql
--
-- 멱등 설계: INSERT IGNORE로 중복 실행해도 안전
-- warehouse/section은 k6 setup()에서 API로 동적 생성하므로 여기에 없음
-- =============================================================

-- AuditorAwareImpl이 항상 1L을 반환하므로
-- inventory_transaction.member_id FK 충족을 위해 member ID=1 필수
INSERT IGNORE INTO member (
    member_id, login_id, password, role,
    member_name, email, phone_number,
    created_by, created_at
) VALUES (
    1, 'k6admin', 'no_password', 'ADMIN',
    'K6테스트관리자', 'k6admin@test.com', '010-1111-1111',
    1, NOW()
);

INSERT IGNORE INTO category (
    category_id, category_code, category_name,
    created_by, created_at
) VALUES (
    1, 'SKC', '스킨케어',
    1, NOW()
);

INSERT IGNORE INTO product_type (
    type_id, type_code, type_name,
    created_by, created_at
) VALUES (
    1, 'TON', '토너',
    1, NOW()
);

INSERT IGNORE INTO partner (
    partner_id, partner_name, partner_type, business_number,
    created_by, created_at
) VALUES (
    1, 'K6성능테스트협력사', 'VENDOR', '111-11-11111',
    1, NOW()
);

-- sku_code: P000001 (ProductService의 P{id:06d} 포맷 준수)
-- temperature_type: ROOM (상온) → k6 창고 ROOM 섹션과 매핑
INSERT IGNORE INTO product (
    product_id, sku_code, brand_name, product_name, product_price,
    temperature_type, category_id, type_id,
    skin_type, volume, unit,
    created_by, created_at
) VALUES (
    1, 'P000001', 'K6브랜드', 'K6성능테스트토너', 15000,
    'ROOM', 1, 1,
    'ALL', 150, 'ml',
    1, NOW()
);

-- =============================================================
-- 확인 쿼리
-- =============================================================
SELECT 'member'       AS table_name, COUNT(*) AS cnt FROM member       WHERE member_id  = 1
UNION ALL
SELECT 'category'     AS table_name, COUNT(*) AS cnt FROM category     WHERE category_id = 1
UNION ALL
SELECT 'product_type' AS table_name, COUNT(*) AS cnt FROM product_type WHERE type_id     = 1
UNION ALL
SELECT 'partner'      AS table_name, COUNT(*) AS cnt FROM partner      WHERE partner_id  = 1
UNION ALL
SELECT 'product'      AS table_name, COUNT(*) AS cnt FROM product      WHERE product_id  = 1;
