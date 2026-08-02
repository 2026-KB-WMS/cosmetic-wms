package com.kb.auth.global.restdocs;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Arrays;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .apply(springSecurity())
                .apply(documentationConfiguration(provider)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    protected static ResponseFieldsSnippet globalErrorResponseFields() {
        return responseFields(
                fieldWithPath("errorCode").description("서버 지정 에러 코드"),
                fieldWithPath("message").description("예외 상세 메시지"),
                fieldWithPath("timestamp").description("예외 발생 시각")
        );
    }

    protected static RequestFieldsSnippet createRequestFields(FieldDescriptor... descriptors) {
        return requestFields(descriptors);
    }

    protected static ResponseFieldsSnippet createResponseFields(FieldDescriptor... descriptors) {
        return responseFields(descriptors);
    }

    protected static ResponseFieldsSnippet createListResponseFields(FieldDescriptor... descriptors) {
        return responseFields(
                Arrays.stream(descriptors)
                        .map(field -> fieldWithPath("[]." + field.getPath())
                                .type(field.getType())
                                .description(field.getDescription())
                                .optional())
                        .toArray(FieldDescriptor[]::new)
        );
    }

    protected static ResourceSnippetParametersBuilder buildParams(String tag, String summary) {
        return ResourceSnippetParameters.builder()
                .tag(tag)
                .summary(summary);
    }

    protected static ResourceSnippetParametersBuilder buildParams(String tag, String summary, String description) {
        return ResourceSnippetParameters.builder()
                .tag(tag)
                .summary(summary)
                .description(description);
    }

    protected static ResourceSnippetParametersBuilder buildParams(String tag, String summary, String reqSchema, String resSchema) {
        ResourceSnippetParametersBuilder builder = ResourceSnippetParameters.builder()
                .tag(tag)
                .summary(summary);
        if (reqSchema != null) builder.requestSchema(Schema.schema(reqSchema));
        if (resSchema != null) builder.responseSchema(Schema.schema(resSchema));
        return builder;
    }

    protected static ResourceSnippetParametersBuilder buildErrorParams(String tag, String summary) {
        return ResourceSnippetParameters.builder()
                .tag(tag)
                .summary(summary)
                .responseSchema(Schema.schema(ApiSchemas.GLOBAL_ERROR_RESPONSE));
    }
}
