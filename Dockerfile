# --- 1단계: 빌드 스테이지 ---
FROM gradle:8-jdk17 AS builder
WORKDIR /app
COPY . .
# 권한 부여 및 빌드
RUN chmod +x ./gradlew && ./gradlew clean build -x test

# --- 2단계: 실행 스테이지 ---
FROM amazoncorretto:17-al2023-headless
WORKDIR /app
# 빌드된 JAR만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]