#Docker Image 빌드를 위한 Base Image 설정
FROM eclipse-temurin:21-jdk

# /tmp 디렉터리를 볼륨으로 설정
VOLUME /tmp

# jar 파일 경로를 지정하는 인자
ARG JAR_FILE=build/libs/*.jar

# 지정된 jar 파일을 이미지 내부의 app.jar로 복사
COPY ${JAR_FILE} app.jar

# prod 프로파일을 사용해 production 설정을 적용
ENV SPRING_PROFILES_ACTIVE=prod

# 컨테이너 시작 시 실행할 명령어 정의
ENTRYPOINT ["java","-jar","/app.jar"]