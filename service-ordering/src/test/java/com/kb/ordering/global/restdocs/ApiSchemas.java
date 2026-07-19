package com.kb.ordering.global.restdocs;

public final class ApiSchemas {

    private ApiSchemas() {
    }

    // Auth
    public static final String SIGN_UP_REQUEST = "SignUpRequest";
    public static final String SIGN_UP_RESPONSE = "SignUpResponse";
    public static final String LOGIN_REQUEST = "LoginRequest";
    public static final String LOGIN_RESPONSE = "LoginResponse";

    // Store
    public static final String STORE_CREATE_REQUEST = "StoreCreateRequest";
    public static final String STORE_RESPONSE = "StoreResponse";

    // Category
    public static final String CATEGORY_CREATE_REQUEST = "CategoryCreateRequest";
    public static final String CATEGORY_RESPONSE = "CategoryResponse";

    // ProductType
    public static final String PRODUCT_TYPE_CREATE_REQUEST = "ProductTypeCreateRequest";
    public static final String PRODUCT_TYPE_RESPONSE = "ProductTypeResponse";

    // Product
    public static final String PRODUCT_CREATE_REQUEST = "ProductCreateRequest";
    public static final String PRODUCT_UPDATE_REQUEST = "ProductUpdateRequest";
    public static final String PRODUCT_SUMMARY_RESPONSE = "ProductSummaryResponse";
    public static final String PRODUCT_DETAIL_RESPONSE = "ProductDetailResponse";

    // Order
    public static final String ORDER_CREATE_REQUEST = "OrderCreateRequest";
    public static final String ORDER_RESPONSE = "OrderResponse";

    // Global
    public static final String GLOBAL_ERROR_RESPONSE = "GlobalErrorResponse";
}
