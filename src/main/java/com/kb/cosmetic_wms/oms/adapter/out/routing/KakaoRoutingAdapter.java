package com.kb.cosmetic_wms.oms.adapter.out.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.application.port.out.RoutingPort;
import com.kb.cosmetic_wms.oms.domain.exception.RoutingFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Kakao Mobility 자동차 길찾기 API 어댑터.
 * 좌표 파라미터는 "경도,위도" 순서를 사용한다.
 */
@Component
public class KakaoRoutingAdapter implements RoutingPort {

    private static final int RESULT_CODE_SUCCESS = 0;

    private final RestClient restClient;

    public KakaoRoutingAdapter(RestClient.Builder restClientBuilder,
                               @Value("${routing.kakao.base-url:https://apis-navi.kakaomobility.com}") String baseUrl,
                               @Value("${geocoding.kakao.api-key:}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    @Override
    public long drivingDistanceMeters(GeoCoordinate origin, GeoCoordinate destination) {
        KakaoDirectionsResponse response = requestDirections(origin, destination);
        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            throw new RoutingFailedException();
        }
        KakaoDirectionsResponse.Route route = response.routes().getFirst();
        if (route.resultCode() != RESULT_CODE_SUCCESS || route.summary() == null) {
            throw new RoutingFailedException();
        }
        return route.summary().distance();
    }

    private KakaoDirectionsResponse requestDirections(GeoCoordinate origin, GeoCoordinate destination) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/directions")
                            .queryParam("origin", toParam(origin))
                            .queryParam("destination", toParam(destination))
                            .build())
                    .retrieve()
                    .body(KakaoDirectionsResponse.class);
        } catch (RoutingFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutingFailedException();
        }
    }

    private String toParam(GeoCoordinate coordinate) {
        return coordinate.longitude() + "," + coordinate.latitude();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoDirectionsResponse(List<Route> routes) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Route(@JsonProperty("result_code") int resultCode, Summary summary) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Summary(long distance) {
        }
    }
}
