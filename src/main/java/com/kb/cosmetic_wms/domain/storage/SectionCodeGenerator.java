package com.kb.cosmetic_wms.domain.storage;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.constants.StorageConstants;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import org.springframework.stereotype.Component;

@Component
public class SectionCodeGenerator {
    public String generate(String warehouseCode, SectionType sectionType, TemperatureType temperatureType, int sequence) {
        validateSequence(sequence);

        String formattedSequence = String.format("%02d", sequence);

        return String.format("%s-%s-%s-%s",
                warehouseCode,
                sectionType.getShortCode(),
                temperatureType.getShortCode(),
                formattedSequence
        );
    }

    private void validateSequence(int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException(StorageConstants.INVALID_SECTION_SEQUENCE_MESSAGE);
        }
    }
}
