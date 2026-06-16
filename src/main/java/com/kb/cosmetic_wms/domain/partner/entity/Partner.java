package com.kb.cosmetic_wms.domain.partner.entity;

import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerValidationException;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "partner",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_partner_business_number", columnNames = "business_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner extends BaseEntity {

    private static final String PARTNER_NAME_REQUIRED_MESSAGE = "파트너명은 필수 항목입니다.";
    private static final String PARTNER_TYPE_REQUIRED_MESSAGE = "파트너 타입은 필수 항목입니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_id")
    private Long id;

    @Column(name = "partner_name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_type", nullable = false, length = 30)
    private PartnerType type;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    private Partner(String name, PartnerType type, String businessNumber) {
        this.name = name;
        this.type = type;
        this.businessNumber = businessNumber;
    }

    public static Partner create(String name, PartnerType type, String businessNumber) {
        validateName(name);
        validateType(type);

        return new Partner(name, type, businessNumber);
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new PartnerValidationException();
        }
    }

    private static void validateType(PartnerType type) {
        if (type == null) {
            throw new PartnerValidationException();
        }
    }
}
