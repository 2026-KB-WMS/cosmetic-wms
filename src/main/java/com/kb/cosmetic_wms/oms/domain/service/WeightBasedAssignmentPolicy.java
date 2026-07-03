package com.kb.cosmetic_wms.oms.domain.service;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.domain.exception.NoAssignableWarehouseException;
import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
import com.kb.cosmetic_wms.oms.domain.exception.OmsValidationException;
import com.kb.cosmetic_wms.oms.domain.model.DemandLine;
import com.kb.cosmetic_wms.oms.domain.model.WarehouseCandidate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 가중치 기반 최적 창고 배정 정책 (순수 도메인 서비스).
 *
 * <pre>
 * 0단계: 발주 품목 전량 충족 가능 창고만 후보로 유지 (출고 라인-재고 정합성 보장)
 * 1단계: 하버사인 직선거리 상위 N개 필터링 (외부 Routing API 호출량 절감)
 * 2단계: 주행거리 산정 — 한 건이라도 실패하면 비교 일관성을 위해 전체 하버사인 폴백
 * 3단계: 거리·FEFO(임박 재고 우선 소진)·SKU 재고 여유율 점수를 가중 합산해 최고점 선택
 * </pre>
 */
public class WeightBasedAssignmentPolicy {

    private static final int DEFAULT_SHORTLIST_SIZE = 5;
    private static final double DEFAULT_DISTANCE_WEIGHT = 0.5;
    private static final double DEFAULT_FEFO_WEIGHT = 0.3;
    private static final double DEFAULT_STOCK_DEPTH_WEIGHT = 0.2;

    /**
     * 요청 수량 대비 가용 수량이 이 배수 이상이면 재고 여유율 만점으로 간주
     */
    private static final double STOCK_DEPTH_SATURATION = 3.0;

    private final int shortlistSize;
    private final double distanceWeight;
    private final double fefoWeight;
    private final double stockDepthWeight;

    public WeightBasedAssignmentPolicy(int shortlistSize, double distanceWeight,
                                       double fefoWeight, double stockDepthWeight) {
        if (shortlistSize <= 0
                || distanceWeight < 0 || fefoWeight < 0 || stockDepthWeight < 0
                || distanceWeight + fefoWeight + stockDepthWeight <= 0) {
            throw new OmsValidationException(OmsErrorCode.INVALID_ASSIGNMENT_POLICY);
        }
        this.shortlistSize = shortlistSize;
        this.distanceWeight = distanceWeight;
        this.fefoWeight = fefoWeight;
        this.stockDepthWeight = stockDepthWeight;
    }

    public static WeightBasedAssignmentPolicy withDefaults() {
        return new WeightBasedAssignmentPolicy(DEFAULT_SHORTLIST_SIZE,
                DEFAULT_DISTANCE_WEIGHT, DEFAULT_FEFO_WEIGHT, DEFAULT_STOCK_DEPTH_WEIGHT);
    }

    public WarehouseCandidate assign(GeoCoordinate destination,
                                     List<WarehouseCandidate> candidates,
                                     List<DemandLine> demands,
                                     LocalDate referenceDate,
                                     DrivingDistanceProvider drivingDistanceProvider) {
        if (destination == null || referenceDate == null || drivingDistanceProvider == null
                || demands == null || demands.isEmpty()) {
            throw new OmsValidationException(OmsErrorCode.INVALID_ASSIGNMENT_INPUT);
        }

        List<WarehouseCandidate> fulfillable = filterFulfillable(candidates, demands);
        List<WarehouseCandidate> shortlist = shortlistByStraightDistance(destination, fulfillable);
        Map<Long, Long> distances = measureDistances(destination, shortlist, drivingDistanceProvider);

        return selectBest(shortlist, distances, demands, referenceDate);
    }

    private List<WarehouseCandidate> filterFulfillable(List<WarehouseCandidate> candidates,
                                                       List<DemandLine> demands) {
        if (candidates == null || candidates.isEmpty()) {
            throw new NoAssignableWarehouseException();
        }
        List<WarehouseCandidate> fulfillable = candidates.stream()
                .filter(candidate -> canFulfillAll(candidate, demands))
                .toList();
        if (fulfillable.isEmpty()) {
            throw new NoAssignableWarehouseException();
        }
        return fulfillable;
    }

    private boolean canFulfillAll(WarehouseCandidate candidate, List<DemandLine> demands) {
        return demands.stream()
                .allMatch(demand -> candidate.availableQuantityOf(demand.productId()) >= demand.quantity());
    }

    private List<WarehouseCandidate> shortlistByStraightDistance(GeoCoordinate destination,
                                                                 List<WarehouseCandidate> candidates) {
        return candidates.stream()
                .sorted(Comparator.comparingLong(
                        c -> HaversineDistanceCalculator.distanceMeters(c.getCoordinate(), destination)))
                .limit(shortlistSize)
                .toList();
    }

    private Map<Long, Long> measureDistances(GeoCoordinate destination,
                                             List<WarehouseCandidate> shortlist,
                                             DrivingDistanceProvider drivingDistanceProvider) {
        Map<Long, Long> distances = new HashMap<>();
        try {
            for (WarehouseCandidate candidate : shortlist) {
                distances.put(candidate.getWarehouseId(),
                        drivingDistanceProvider.drivingDistanceMeters(candidate.getCoordinate(), destination));
            }
            return distances;
        } catch (RuntimeException e) {
            return fallbackToHaversine(destination, shortlist);
        }
    }

    private Map<Long, Long> fallbackToHaversine(GeoCoordinate destination,
                                                List<WarehouseCandidate> shortlist) {
        Map<Long, Long> distances = new HashMap<>();
        for (WarehouseCandidate candidate : shortlist) {
            distances.put(candidate.getWarehouseId(),
                    HaversineDistanceCalculator.distanceMeters(candidate.getCoordinate(), destination));
        }
        return distances;
    }

    private WarehouseCandidate selectBest(List<WarehouseCandidate> shortlist,
                                          Map<Long, Long> distances,
                                          List<DemandLine> demands,
                                          LocalDate referenceDate) {
        long minDistance = distances.values().stream().mapToLong(Long::longValue).min().orElseThrow();
        long maxDistance = distances.values().stream().mapToLong(Long::longValue).max().orElseThrow();

        Map<Long, Double> expiryDays = new HashMap<>();
        for (WarehouseCandidate candidate : shortlist) {
            expiryDays.put(candidate.getWarehouseId(), weightedDaysToExpiry(candidate, demands, referenceDate));
        }
        double minExpiryDays = expiryDays.values().stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double maxExpiryDays = expiryDays.values().stream().mapToDouble(Double::doubleValue).max().orElseThrow();

        WarehouseCandidate best = null;
        double bestScore = -1;
        for (WarehouseCandidate candidate : shortlist) {
            double distanceScore = invertedMinMax(distances.get(candidate.getWarehouseId()), minDistance, maxDistance);
            double fefoScore = invertedMinMax(expiryDays.get(candidate.getWarehouseId()), minExpiryDays, maxExpiryDays);
            double stockDepthScore = stockDepthScore(candidate, demands);

            double score = distanceWeight * distanceScore
                    + fefoWeight * fefoScore
                    + stockDepthWeight * stockDepthScore;

            if (best == null || score > bestScore
                    || (score == bestScore && tieBreak(candidate, best, distances))) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    /**
     * FEFO 점수의 원천값 — 요청 수량으로 가중한 평균 잔여 유통기한(일).
     * 값이 작을수록(임박 재고 보유) 우선 배정해 폐기 리스크를 줄인다.
     */
    private double weightedDaysToExpiry(WarehouseCandidate candidate,
                                        List<DemandLine> demands,
                                        LocalDate referenceDate) {
        long totalQuantity = 0;
        double weightedDays = 0;
        for (DemandLine demand : demands) {
            LocalDate expiry = candidate.earliestExpiryOf(demand.productId());
            long days = Math.max(0, ChronoUnit.DAYS.between(referenceDate, expiry));
            weightedDays += days * (double) demand.quantity();
            totalQuantity += demand.quantity();
        }
        return weightedDays / totalQuantity;
    }

    /**
     * SKU 충족률 점수 — 전량 충족은 0단계에서 보장되므로,
     * 요청 수량 대비 가용 재고 배수(여유율)를 포화 상한까지 정규화해 재고가 많은 창고를 우대한다.
     */
    private double stockDepthScore(WarehouseCandidate candidate, List<DemandLine> demands) {
        long totalQuantity = 0;
        double weightedRatio = 0;
        for (DemandLine demand : demands) {
            double ratio = (double) candidate.availableQuantityOf(demand.productId()) / demand.quantity();
            weightedRatio += Math.min(ratio, STOCK_DEPTH_SATURATION) * demand.quantity();
            totalQuantity += demand.quantity();
        }
        return (weightedRatio / totalQuantity) / STOCK_DEPTH_SATURATION;
    }

    private double invertedMinMax(double value, double min, double max) {
        if (max == min) {
            return 1.0;
        }
        return (max - value) / (max - min);
    }

    private boolean tieBreak(WarehouseCandidate challenger, WarehouseCandidate current,
                             Map<Long, Long> distances) {
        long challengerDistance = distances.get(challenger.getWarehouseId());
        long currentDistance = distances.get(current.getWarehouseId());
        if (challengerDistance != currentDistance) {
            return challengerDistance < currentDistance;
        }
        return challenger.getWarehouseId() < current.getWarehouseId();
    }
}
