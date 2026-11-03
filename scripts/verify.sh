#!/usr/bin/env bash
# 완료 선언 전 실행하는 전체 검증: clean build + 전체 테스트 + OpenAPI 생성
# 통합 테스트는 Testcontainers(MySQL)를 쓰므로 Docker가 실행 중이어야 한다. 스킵된 테스트가 있으면 실패로 본다.
set -euo pipefail

cd "$(dirname "$0")/.."

./gradlew clean build

python3 - <<'PY'
import glob
import sys
import xml.etree.ElementTree as ET

total = failed = skipped = 0
for path in glob.glob("build/test-results/test/*.xml"):
    root = ET.parse(path).getroot()
    total += int(root.get("tests"))
    failed += int(root.get("failures")) + int(root.get("errors"))
    skipped += int(root.get("skipped"))

print(f"tests={total} failed={failed} skipped={skipped}")
if total == 0 or failed or skipped:
    sys.exit(1)
PY

python3 - <<'PY'
import os
import xml.etree.ElementTree as ET

path = "build/reports/jacoco/test/jacocoTestReport.xml"
if not os.path.exists(path):
    print("coverage: 리포트 없음")
else:
    counters = {c.get("type"): c for c in ET.parse(path).getroot().findall("counter")}
    parts = []
    for kind, label in (("LINE", "line"), ("BRANCH", "branch")):
        c = counters.get(kind)
        if c is not None:
            covered, missed = int(c.get("covered")), int(c.get("missed"))
            parts.append(f"{label} {covered / (covered + missed) * 100:.1f}% ({covered}/{covered + missed})")
    print("coverage: " + ", ".join(parts) + "  (리포트: build/reports/jacoco/test/html/index.html)")
PY

echo "VERIFY OK"
