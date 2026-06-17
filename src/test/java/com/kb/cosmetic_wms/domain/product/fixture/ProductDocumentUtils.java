package com.kb.cosmetic_wms.domain.product.fixture;

import org.springframework.restdocs.payload.RequestFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class ProductDocumentUtils {

    private ProductDocumentUtils() {
    }

    public static RequestFieldsSnippet getProductCreateRequestFields() {
        return requestFields(
                fieldWithPath("brandName").description("브랜드명")
                        .attributes(key("example").value("BIO")),
                fieldWithPath("productName").description("상품명")
                        .attributes(key("example").value("하이드라비오 토너")),
                fieldWithPath("productPrice").description("상품 가격 (0원 이상)")
                        .attributes(key("example").value(15000)),
                fieldWithPath("temperatureType").description("보관 온도 타입 (ROOM: 상온, COOL: 냉장)")
                        .attributes(key("example").value("ROOM")),
                fieldWithPath("categoryId").description("카테고리 ID")
                        .attributes(key("example").value(1)),
                fieldWithPath("productTypeId").description("상품 타입 ID")
                        .attributes(key("example").value(1)),
                fieldWithPath("productInfo.skinType").description("피부 타입 (선택)").optional()
                        .attributes(key("example").value("건성")),
                fieldWithPath("productInfo.functionType").description("기능성 타입 (선택)").optional()
                        .attributes(key("example").value("보습")),
                fieldWithPath("productInfo.volume.value").description("용량 수치 (1 이상)")
                        .attributes(key("example").value(150)),
                fieldWithPath("productInfo.volume.unit").description("용량 단위 (ml, g, ea, oz, fl.oz)")
                        .attributes(key("example").value("ml")),
                fieldWithPath("productInfo.ingredients").description("성분 정보 (선택)").optional()
                        .attributes(key("example").value("정제수, 글리세린")),
                fieldWithPath("productInfo.cautions").description("사용 주의사항 (선택)").optional()
                        .attributes(key("example").value("직사광선 주의")),
                fieldWithPath("productInfo.storageCondition").description("보관 조건 (선택)").optional()
                        .attributes(key("example").value("상온보관"))
        );
    }
}
