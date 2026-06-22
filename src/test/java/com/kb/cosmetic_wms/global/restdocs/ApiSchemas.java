package com.kb.cosmetic_wms.global.restdocs;

public final class ApiSchemas {

    private ApiSchemas() {
    }

    // Storage
    public static final String WAREHOUSE_CREATE_REQUEST = "WarehouseCreateRequest";
    public static final String WAREHOUSE_RESPONSE = "WarehouseResponse";
    public static final String SECTION_CREATE_REQUEST = "SectionCreateRequest";

    // Partner
    public static final String PARTNER_CREATE_REQUEST = "PartnerCreateRequest";
    public static final String PARTNER_RESPONSE = "PartnerResponse";

    // Store
    public static final String STORE_CREATE_REQUEST = "StoreCreateRequest";
    public static final String STORE_RESPONSE = "StoreResponse";

    // Member
    public static final String MEMBER_SIGNUP_REQUEST = "MemberSignUpRequest";
    public static final String MEMBER_LOGIN_REQUEST = "MemberLoginRequest";
    public static final String MEMBER_DETAIL_RESPONSE = "MemberDetailResponse";

    // Product
    public static final String PRODUCT_CREATE_REQUEST = "ProductCreateRequest";
    public static final String PRODUCT_UPDATE_REQUEST = "ProductUpdateRequest";
    public static final String PRODUCT_SUMMARY_RESPONSE = "ProductSummaryResponse";
    public static final String PRODUCT_DETAIL_RESPONSE = "ProductDetailResponse";

    // Lot
    public static final String LOT_CREATE_REQUEST = "LotCreateRequest";
    public static final String LOT_STATUS_UPDATE_REQUEST = "LotStatusUpdateRequest";
    public static final String LOT_DETAIL_RESPONSE = "LotDetailResponse";

    // Inventory
    public static final String INVENTORY_STATUS_CHANGE_REQUEST = "InventoryStatusChangeRequest";
    public static final String INVENTORY_DETAIL_RESPONSE = "InventoryDetailResponse";

    // Inbound
    public static final String INBOUND_CREATE_REQUEST = "InboundCreateRequest";
    public static final String INBOUND_DETAIL_RESPONSE = "InboundDetailResponse";
    public static final String INBOUND_ITEM_ADD_REQUEST = "InboundItemAddRequest";
    public static final String INBOUND_PUTAWAY_REQUEST = "InboundPutawayRequest";
    public static final String INBOUND_ITEM_RESPONSE = "InboundItemResponse";

    // Inspection
    public static final String INSPECTION_START_REQUEST = "InspectionStartRequest";
    public static final String INSPECTION_COMPLETE_REQUEST = "InspectionCompleteRequest";
    public static final String INSPECTION_DETAIL_RESPONSE = "InspectionDetailResponse";

    // Order
    public static final String ORDER_CREATE_REQUEST = "OrderCreateRequest";
    public static final String ORDER_RESPONSE = "OrderResponse";

    // Global
    public static final String GLOBAL_ERROR_RESPONSE = "GlobalErrorResponse";
}
