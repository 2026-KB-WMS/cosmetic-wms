package com.kb.cosmetic_wms.partner.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.partner.domain.model.Partner;
import com.kb.cosmetic_wms.partner.domain.model.PartnerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "partner",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_partner_business_number", columnNames = "business_number")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PartnerEntity extends BaseEntity {

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

    private PartnerEntity(String name, PartnerType type, String businessNumber) {
        this.name = name;
        this.type = type;
        this.businessNumber = businessNumber;
    }

    static PartnerEntity fromDomain(Partner partner) {
        return new PartnerEntity(partner.getName(), partner.getType(), partner.getBusinessNumber());
    }

    Partner toDomain() {
        return Partner.reconstitute(id, name, type, businessNumber);
    }
}