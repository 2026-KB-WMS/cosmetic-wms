package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import com.kb.cosmetic_wms.global.config.JpaConfig;
import com.kb.cosmetic_wms.storage.adapter.out.persistence.StoragePersistenceAdapter;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, StoragePersistenceAdapter.class})
public class WarehouseRepositoryTest {

    @Autowired
    private StoragePersistenceAdapter storagePersistenceAdapter;

    @Test
    void 창고와_하위_보관_섹션을_함께_저장하면_Cascade가_작동하여_한_번에_영속화된다() {
        Warehouse warehouse = new WarehouseTestBuilder().build();

        warehouse.addStorageSection(new SectionCode("WH01-HIGH-R-01"), "A동 상단 랙", SectionType.HIGH_ROT, TemperatureType.ROOM, 3000);
        warehouse.addDockingSection(new SectionCode("WH01-DOCK-C-01"), "1번 검수장 도크", TemperatureType.COOL, 2000);

        Warehouse saved = storagePersistenceAdapter.save(warehouse);

        assertThat(saved.getWarehouseId()).isNotNull();
        assertThat(saved.getSections()).hasSize(2);
        assertThat(saved.getSections().get(0).getSectionId()).isNotNull();
        assertThat(saved.getSections().get(1).getSectionCode().value()).isEqualTo("WH01-DOCK-C-01");
    }
}