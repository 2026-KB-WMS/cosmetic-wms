# 미리 빌드한 실행 가능 jar를 담는다: ./gradlew bootJar
# 빌드 단계를 이미지 안에 두지 않는 이유: 빌드가 Testcontainers 테스트를 실행하는데 컨테이너 안에서는 Docker를 쓸 수 없다.
FROM eclipse-temurin:21-jre

RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY build/libs/*.jar app.jar

USER app
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
