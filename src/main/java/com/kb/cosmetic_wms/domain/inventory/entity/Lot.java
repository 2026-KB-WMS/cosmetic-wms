package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.LotConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.LotStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String lotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private LotStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Lot(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Product product
    ) {
        this.lotNumber = lotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.status = LotStatus.AVAILABLE;
        this.product = product;
    }

    public static Lot create(
            String lotNumber, LocalDateTime manufacturingDate,
            LocalDateTime expirationDate, Product product
    ) {
        validateDates(manufacturingDate, expirationDate);
        validateLotNo(lotNumber);

        return new Lot(lotNumber, manufacturingDate, expirationDate, product);
    }

    private static void validateDates(LocalDateTime manufacturingDate, LocalDateTime expirationDate) {
        if (manufacturingDate == null || expirationDate == null) {
            throw new IllegalArgumentException("제조일자와 유통기한은 필수 입력 값입니다.");
        }

        if (manufacturingDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("제조일자는 유통기한보다 미래일 수 없습니다.");
        }
    }

    private static void validateLotNo(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new IllegalArgumentException("로트 번호는 필수 입력 값입니다.");
        }

        if (!LotConstants.LOT_NO_PATTERN.matcher(lotNumber).matches()) {
            throw new IllegalArgumentException("올바르지 않은 로트 번호 형식입니다. (규격: [카테고리3자]-[YYMMDD]-[공장2자]-[일련번호4자])");
        }
    }
}
