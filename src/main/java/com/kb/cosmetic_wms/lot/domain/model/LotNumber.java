package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotNumberFormatException;
import com.kb.cosmetic_wms.lot.domain.exception.LotNumberRequiredException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

// [입고일자 YYMMDD]-[입고ID]-[제조사 로트 번호]  예: 260629-1042-LOT0001
public record LotNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\d{6}-\\d{1,19}-[A-Z0-9][A-Z0-9\\-]{0,19}$");
    private static final Pattern MANUFACTURER_LOT_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9\\-]{0,19}$");
    private static final DateTimeFormatter INBOUND_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    public LotNumber {
        if (value == null || value.isBlank()) {
            throw new LotNumberRequiredException();
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidLotNumberFormatException();
        }
    }

    public static LotNumber of(LocalDate inboundDate, Long inboundId, String manufacturerLotNumber) {
        if (manufacturerLotNumber == null || !MANUFACTURER_LOT_PATTERN.matcher(manufacturerLotNumber).matches()) {
            throw new InvalidLotNumberFormatException();
        }
        String assembled = String.format("%s-%d-%s",
                inboundDate.format(INBOUND_DATE_FORMATTER), inboundId, manufacturerLotNumber);
        return new LotNumber(assembled);
    }
}