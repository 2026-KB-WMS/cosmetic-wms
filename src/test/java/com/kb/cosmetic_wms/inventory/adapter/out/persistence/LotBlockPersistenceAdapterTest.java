package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.global.config.JpaConfig;
import com.kb.cosmetic_wms.support.MySqlContainerSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, LotBlockPersistenceAdapter.class})
class LotBlockPersistenceAdapterTest extends MySqlContainerSupport {

    @Autowired
    private LotBlockPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM inventory_lot_block");
    }

    private int rowCount(long lotId) {
        entityManager.flush();
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_lot_block WHERE lot_id = ?", Integer.class, lotId);
    }

    private String storedStatus(long lotId) {
        entityManager.flush();
        return jdbcTemplate.queryForObject(
                "SELECT status FROM inventory_lot_block WHERE lot_id = ?", String.class, lotId);
    }

    @Test
    void 차단하지_않은_로트는_차단된_것으로_판단하지_않는다() {
        assertThat(adapter.isBlocked(10L)).isFalse();
    }

    @Test
    void 로트를_차단하면_사유와_함께_저장되고_차단된_것으로_판단한다() {
        adapter.block(10L, "RECALLED");

        assertThat(adapter.isBlocked(10L)).isTrue();
        assertThat(storedStatus(10L)).isEqualTo("RECALLED");
    }

    @Test
    void 이미_차단된_로트를_다시_차단하면_행은_하나로_유지되고_사유만_갱신된다() {
        adapter.block(10L, "HOLD");
        adapter.block(10L, "RECALLED");

        assertThat(rowCount(10L)).isEqualTo(1);
        assertThat(storedStatus(10L)).isEqualTo("RECALLED");
    }

    @Test
    void 차단을_해제하면_더_이상_차단된_것으로_판단하지_않는다() {
        adapter.block(10L, "HOLD");

        adapter.release(10L);

        assertThat(adapter.isBlocked(10L)).isFalse();
        assertThat(rowCount(10L)).isZero();
    }

    @Test
    void 차단되지_않은_로트의_해제는_예외_없이_무시된다() {
        assertThatCode(() -> adapter.release(99L)).doesNotThrowAnyException();
    }

    @Test
    void 한_로트의_차단과_해제는_다른_로트에_영향을_주지_않는다() {
        adapter.block(10L, "HOLD");
        adapter.block(20L, "DAMAGED");

        adapter.release(10L);

        assertThat(adapter.isBlocked(10L)).isFalse();
        assertThat(adapter.isBlocked(20L)).isTrue();
    }
}
