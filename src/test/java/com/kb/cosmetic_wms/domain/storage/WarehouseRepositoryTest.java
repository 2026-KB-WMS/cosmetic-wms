package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.domain.storage.repository.WarehouseRepository;
import com.kb.cosmetic_wms.global.config.JpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class WarehouseRepositoryTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    void 창고와_하위_보관_섹션을_함께_저장하면_Cascade가_작동하여_한_번에_영속화된다() {
        // given
        Warehouse warehouse = new WarehouseTestBuilder().build();

        warehouse.addStorageSection("WH01-HIGH-R-01", "A동 상단 랙", SectionType.HIGH_ROT, TemperatureType.ROOM, 3000);
        warehouse.addDockingSection("WH01-DOCK-C-01", "1번 검수장 도크", TemperatureType.COOL, 2000);

        // when
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        // then
        assertThat(savedWarehouse.getId()).isNotNull();
        assertThat(savedWarehouse.getSections()).hasSize(2);
        assertThat(savedWarehouse.getSections().get(0).getId()).isNotNull();
        assertThat(savedWarehouse.getSections().get(1).getSectionCode()).isEqualTo("WH01-DOCK-C-01");
    }
}
