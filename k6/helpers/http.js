import http from 'k6/http';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const JSON_HEADERS = {
    'Content-Type': 'application/json',
};

// 기본 요청
const defaultParams = {
    headers: JSON_HEADERS,
};

// Polling 요청 (404도 정상)
const pollingParams = {
    headers: JSON_HEADERS,
    responseCallback: http.expectedStatuses(200, 404),
};

function logIfFailed(method, url, res) {
    // polling의 404는 정상 응답으로 간주
    if (res.status >= 400 && !(method === 'GET(POLL)' && res.status === 404)) {
        console.error(
            `[${method}] ${url}
status=${res.status}
body=${res.body}`
        );
    }
}

export function post(url, body, params = {}) {
    const res = http.post(
        `${BASE_URL}${url}`,
        JSON.stringify(body),
        {
            ...defaultParams,
            ...params,
        }
    );

    logIfFailed('POST', url, res);
    return res;
}

export function patch(url, body, params = {}) {
    const res = http.patch(
        `${BASE_URL}${url}`,
        JSON.stringify(body),
        {
            ...defaultParams,
            ...params,
        }
    );

    logIfFailed('PATCH', url, res);
    return res;
}

export function get(url, params = {}) {
    const res = http.get(
        `${BASE_URL}${url}`,
        {
            ...defaultParams,
            ...params,
        }
    );

    logIfFailed('GET', url, res);
    return res;
}

export function getPolling(url, params = {}) {
    const res = http.get(
        `${BASE_URL}${url}`,
        {
            ...pollingParams,
            ...params,
        }
    );

    logIfFailed('GET(POLL)', url, res);
    return res;
}