package com.kb.cosmetic_wms.global.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class KakaoGeocodingAdapter implements GeocodingPort {

    private final RestClient restClient;

    public KakaoGeocodingAdapter(RestClient.Builder restClientBuilder,
                                 @Value("${geocoding.kakao.base-url:https://dapi.kakao.com}") String baseUrl,
                                 @Value("${geocoding.kakao.api-key:}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    @Override
    public GeoCoordinate geocode(String address) {
        KakaoAddressResponse response = requestAddressSearch(address);
        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            throw new GeocodingFailedException(GeocodingErrorCode.GEOCODING_ADDRESS_NOT_FOUND);
        }
        KakaoAddressResponse.Document document = response.documents().getFirst();
        return new GeoCoordinate(new BigDecimal(document.y()), new BigDecimal(document.x()));
    }

    private KakaoAddressResponse requestAddressSearch(String address) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .retrieve()
                    .body(KakaoAddressResponse.class);
        } catch (GeocodingFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new GeocodingFailedException(GeocodingErrorCode.GEOCODING_API_ERROR);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoAddressResponse(List<Document> documents) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Document(String x, String y) {
        }
    }
}
