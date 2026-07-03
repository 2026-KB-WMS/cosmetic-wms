/**
 * 입고 수령 → 품질검사 → 재고 반영 전체 플로우 성능 테스트
 *
 * 실행 방법:
 *   k6 run k6/scenarios/inbound-flow.js
 *
 * 대상 서버 변경:
 *   k6 run --env BASE_URL=http://prod-server:8080 k6/scenarios/inbound-flow.js
 *
 * VU 수 오버라이드:
 *   k6 run --vus 50 --duration 2m k6/scenarios/inbound-flow.js
 *
 * 사전 조건:
 *   1. 앱 실행 중 (localhost:8080)
 *   2. k6/data/seed.sql 적재 완료 (partnerId=1, productId=1)
 */

import {check, fail, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';
import {get, getPolling, patch, post} from '../helpers/http.js';

// ── 커스텀 메트릭 ────────────────────────────────────────────────
// 전체 플로우(5단계) 성공 여부
const flowSuccessRate = new Rate('flow_success_rate');
// 비동기 검사 전표 생성까지 걸린 시간 (polling 소요 시간)
const inspectionPollDuration = new Trend('inspection_poll_duration_ms', true);

// ── 부하 프로파일 ────────────────────────────────────────────────
export const options = {
    stages: [
        {duration: '30s', target: 50}, // 워밍업
        {duration: '1m', target: 150}, // 본 테스트
        {duration: '2m', target: 300},
        {duration: '2m', target: 400},
        {duration: '30s', target: 0}, // 종료: 0 VU로 점진 감소
    ],
    thresholds: {
        // 개별 HTTP 요청 응답시간
        http_req_duration: ['p(95)<2000'],  // 95% 요청이 2초 이내
        // HTTP 레벨 에러율 (4xx/5xx)
        http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
        // 5단계 전체 플로우 성공률
        flow_success_rate: ['rate>0.98'],   // 전체 플로우 성공률 98% 이상
        // 비동기 검사 전표 생성 대기 시간
        inspection_poll_duration_ms: ['p(95)<10000'], // 비동기 생성 시간 10초 이내
    },
};

// ── setup(): 모든 VU 시작 전 1회 실행 ───────────────────────────
export function setup() {
    const listRes = get('/api/v1/storages/warehouses', {
        tags: {
            name: 'GET /api/v1/storages/warehouses',
        },
    });
    check(listRes, {'warehouse list ok': r => r.status === 200});

    const warehouses = JSON.parse(listRes.body);
    const existing = warehouses.find(w => w.warehouseName === 'K6성능테스트창고');

    if (existing) {
        const REQUIRED = ['DOCKING', 'STORAGE', 'QUARANTINE'];
        const hasAll = REQUIRED.every(type => existing.sections.some(s => s.sectionType === type));
        if (hasAll) {
            console.log(`기존 창고 재사용 — warehouseId: ${existing.warehouseId}`);
            return {warehouseId: existing.warehouseId, partnerId: 1, productId: 1};
        }
        fail(
            `창고(warehouseId=${existing.warehouseId})에 필요한 섹션이 누락되어 있습니다. ` +
            'DB에서 해당 창고를 삭제 후 k6를 재실행하세요.'
        );
    }

    // 창고 생성
    // capacity는 하위 섹션 3개의 maxCapacity 합산(29,999,997)보다 충분히 크게 설정해야 함
    const whRes = post('/api/v1/storages/warehouses', {
        warehouseName: 'K6성능테스트창고',
        address: '서울시 성동구 테스트로 1',
        targetTemp: '15~25도',
        capacity: 99999999,
    }, {
        tags: {
            name: 'POST /api/v1/storages/warehouses',
        },
    });
    if (!check(whRes, {'warehouse created (201)': r => r.status === 201})) {
        fail(`창고 생성 실패 (status=${whRes.status}): ${whRes.body}`);
    }
    const warehouseId = JSON.parse(whRes.body).warehouseId;
    console.log(`창고 생성 완료 — warehouseId: ${warehouseId}`);

    // 섹션 3종 생성 (maxCapacity 합계 = 29,999,997 ≤ warehouse capacity 99,999,999)
    const sections = [
        {sectionType: 'DOCKING', sectionName: 'K6 도킹 구역', temperatureType: 'ROOM', maxCapacity: 9999999},
        {sectionType: 'STORAGE', sectionName: 'K6 보관 구역', temperatureType: 'ROOM', maxCapacity: 9999999},
        {sectionType: 'QUARANTINE', sectionName: 'K6 격리 구역', temperatureType: 'ROOM', maxCapacity: 9999999},
    ];
    for (const section of sections) {
        const res = post(
            `/api/v1/storages/warehouses/${warehouseId}/sections`,
            section,
            {
                tags: {
                    name: 'POST /api/v1/storages/warehouses/{id}/sections',
                },
            }
        );
        if (!check(res, {[`section ${section.sectionType} created`]: r => r.status === 200})) {
            fail(`섹션 생성 실패 (${section.sectionType}, status=${res.status}): ${res.body}`);
        }
    }

    return {warehouseId, partnerId: 1, productId: 1};
}

// ── default(): 각 VU가 반복 실행하는 메인 시나리오 ──────────────
export default function (data) {
    const {warehouseId, partnerId, productId} = data;

    // ── Step 1: 입고 등록 ────────────────────────────────────────
    const registerRes = post(
        '/api/v1/inbounds',
        {
            warehouseId,
            partnerId,
            inboundDate: '2027-01-01T00:00:00',
            lines: [{productId, orderedQuantity: 10}],
        },
        {
            tags: {
                name: 'POST /api/v1/inbounds',
            },
        }
    );
    if (!check(registerRes, {'[1] inbound registered (201)': r => r.status === 201})) {
        flowSuccessRate.add(false);
        return;
    }

    const inbound = JSON.parse(registerRes.body);
    const inboundId = inbound.id;
    const lineId = inbound.lines[0].id;

    // ── Step 2: 입고 수령 ────────────────────────────────────────
    // manufacturerLotNumber: VU+ITER+epoch초 조합으로 유니크 보장 (최대 20자, 대문자/숫자/하이픈만 허용)
    const lotSuffix = Math.floor(Date.now() / 1000) % 100000;
    const receiveRes = patch(
        `/api/v1/inbounds/${inboundId}/receive`,
        {
            lines: [{
                lineId,
                receivedQuantity: 10,
                manufacturerLotNumber: `K6-${__VU}-${__ITER}-${lotSuffix}`,
                manufacturingDate: '2025-01-01T00:00:00',
                expirationDate: '2027-12-31T00:00:00',
            }],
        },
        {
            tags: {
                name: 'PATCH /api/v1/inbounds/{id}/receive',
            },
        }
    );
    if (!check(receiveRes, {'[2] inbound received (200)': r => r.status === 200})) {
        flowSuccessRate.add(false);
        return;
    }

    // ── Step 3: 검사 전표 생성 대기 (AFTER_COMMIT + @Async 비동기) ──
    // RetryableInspectionCreator가 비동기로 생성하므로 polling 필요
    // 최대 10초(20회 × 0.5s) 대기 후 타임아웃 처리
    const pollStart = Date.now();
    let inspectionId = null;
    let lotId = null;

    for (let attempt = 0; attempt < 20; attempt++) {
        sleep(0.5);
        const pollRes = getPolling(
            `/api/v1/quality-inspections?sourceType=INBOUND&sourceId=${lineId}`,
            {
                tags: {
                    name: 'GET /api/v1/quality-inspections',
                },
            }
        );

        if (pollRes.status === 404) {
            continue;
        }

        if (!check(pollRes, {
            'poll success': (r) => r.status === 200,
        })) {
            break;
        }

        const body = JSON.parse(pollRes.body);
        inspectionId = body.id;
        lotId = body.lotId;
        break;
    }

    inspectionPollDuration.add(Date.now() - pollStart);

    if (!check(inspectionId, {'[3] inspection created': v => v !== null})) {
        flowSuccessRate.add(false);
        return;
    }

    // ── Step 4: 검사 시작 ────────────────────────────────────────
    const startRes = patch(
        `/api/v1/quality-inspections/${inspectionId}/start`,
        {
            inspectorId: 1,
        },
        {
            tags: {
                name: 'PATCH /api/v1/quality-inspections/{id}/start',
            },
        }
    );
    if (!check(startRes, {'[4] inspection started (200)': r => r.status === 200})) {
        flowSuccessRate.add(false);
        return;
    }

    // ── Step 5: 검사 완료 (전량 합격) ────────────────────────────
    const completeRes = patch(
        `/api/v1/quality-inspections/${inspectionId}/complete`,
        {
            passedQuantity: 10,
            failedQuantity: 0,
            defectReason: null,
        },
        {
            tags: {
                name: 'PATCH /api/v1/quality-inspections/{id}/complete',
            },
        }
    );
    if (!check(completeRes, {'[5] inspection completed (200)': r => r.status === 200})) {
        flowSuccessRate.add(false);
        return;
    }

    // ── Step 6: 재고 반영 검증 ───────────────────────────────────
    // 검사 완료 이벤트는 BEFORE_COMMIT 단계에서 동기 처리
    const invRes = get(
        `/api/v1/inventories?lotId=${lotId}`,
        {
            tags: {
                name: 'GET /api/v1/inventories',
            },
        }
    );
    const inventoryOk = check(invRes, {
        '[6] inventory reflected (200)': r => r.status === 200,
        '[6] normal stock exists': r => {
            if (r.status !== 200) return false;
            const items = JSON.parse(r.body);
            return items.some(i => i.qualityStatus === 'NORMAL');
        },
    });

    flowSuccessRate.add(inventoryOk);
    sleep(1);
}

// ── teardown(): 모든 VU 종료 후 1회 실행 ────────────────────────
export function teardown(data) {
    console.log(`테스트 종료 — warehouseId: ${data.warehouseId}`);
    console.log('DB 정리가 필요하면 수동으로 수행하거나 seed.sql을 재실행하세요.');
}
