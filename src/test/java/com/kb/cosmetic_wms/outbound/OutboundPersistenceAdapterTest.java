package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.global.config.JpaConfig;
import com.kb.cosmetic_wms.outbound.adapter.out.persistence.OutboundPersistenceAdapter;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, OutboundPersistenceAdapter.class})
@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")
class OutboundPersistenceAdapterTest extends MySqlContainerSupport {

    @Autowired
    private OutboundPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM outbound_item");
        jdbcTemplate.update("DELETE FROM outbound");
    }

    private long insertOutbound(String status) {
        jdbcTemplate.update("""
                INSERT INTO outbound (orders_id, warehouse_id, outbound_type, outbound_status, created_by, created_at)
                VALUES (1, 1, 'ORDER', ?, 0, NOW())
                """, status);
        return jdbcTemplate.queryForObject("SELECT MAX(outbound_id) FROM outbound", Long.class);
    }

    @Test
    void 지정한_ID_중_지정한_상태의_출고만_ID_순서로_조회한다() {
        long allocated = insertOutbound("ALLOCATED");
        long processing = insertOutbound("PROCESSING");
        long shipped = insertOutbound("SHIPPED");
        long canceled = insertOutbound("CANCELED");
        long notRequested = insertOutbound("ALLOCATED");

        List<Outbound> result = adapter.findByIdsAndStatuses(
                List.of(processing, canceled, allocated, shipped),
                Set.of(OutboundStatus.ALLOCATED, OutboundStatus.PROCESSING));

        assertThat(result).extracting(Outbound::getId).containsExactly(allocated, processing);
        assertThat(result).extracting(Outbound::getId).doesNotContain(notRequested);
    }

    @Test
    void 일치하는_출고가_없으면_빈_목록을_반환한다() {
        long shipped = insertOutbound("SHIPPED");

        assertThat(adapter.findByIdsAndStatuses(List.of(shipped), Set.of(OutboundStatus.ALLOCATED))).isEmpty();
    }
}
