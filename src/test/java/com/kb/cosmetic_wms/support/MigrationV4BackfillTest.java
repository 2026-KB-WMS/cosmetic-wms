package com.kb.cosmetic_wms.support;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V4 마이그레이션이 기존 로트의 상태를 차단 목록에 반영하는지 검증한다.
 * 빈 DB에서는 lot이 비어 있어 backfill SQL이 실행되지 않으므로, V3까지 적용한 DB에 데이터를 넣고 V4를 적용한다.
 */
class MigrationV4BackfillTest extends MySqlContainerSupport {

    private static final String DATABASE = "migration_v4_backfill";

    @Test
    void 이미_정상이_아닌_로트는_V4_적용_시_차단_목록에_반영되고_정상_로트는_반영되지_않는다() throws SQLException {
        // given — V3까지 적용된 DB에 상태가 다른 로트들이 있다
        try (Connection root = DriverManager.getConnection(rootJdbcUrl(""), "root", rootPassword());
             Statement statement = root.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
            statement.execute("CREATE DATABASE " + DATABASE);
        }
        String url = rootJdbcUrl(DATABASE);

        Flyway.configure().dataSource(url, "root", rootPassword())
                .locations("classpath:db/migration").target("3").load().migrate();

        try (Connection connection = DriverManager.getConnection(url, "root", rootPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            insertLot(statement, 1, "AVAILABLE");
            insertLot(statement, 2, "HOLD");
            insertLot(statement, 3, "RECALLED");
            insertLot(statement, 4, "DISPOSED");
        }

        // when
        Flyway.configure().dataSource(url, "root", rootPassword())
                .locations("classpath:db/migration").load().migrate();

        // then
        Map<Long, String> blocked = new LinkedHashMap<>();
        try (Connection connection = DriverManager.getConnection(url, "root", rootPassword());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT lot_id, status FROM inventory_lot_block ORDER BY lot_id")) {
            while (rs.next()) {
                blocked.put(rs.getLong("lot_id"), rs.getString("status"));
            }
        }
        assertThat(blocked).containsExactly(
                Map.entry(2L, "HOLD"), Map.entry(3L, "RECALLED"), Map.entry(4L, "DISPOSED"));
    }

    private void insertLot(Statement statement, long inboundId, String status) throws SQLException {
        statement.execute("""
                INSERT INTO lot (inbound_id, manufacturer_lot_no, manufacturing_date, expiration_date, status,
                                 product_id, created_by, created_at)
                VALUES (%d, 'LOT-%d', '2026-01-01', '2030-01-01', '%s', 1, 0, NOW())
                """.formatted(inboundId, inboundId, status));
    }
}
