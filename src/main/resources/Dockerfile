FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
# 빌드된 JAR 파일의 경로를 지정하는 변수 (Gradle 빌드의 기본 출력 경로)

COPY ${JAR_FILE} app.jar
# 빌드된 JAR 파일을 컨테이너의 app.jar로 복사

ENTRYPOINT ["java","-jar","/app.jar"]
# 컨테이너가 시작될 때 app.jar 파일을 실행하도록 설정

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
# 컨테이너의 타임존을 서울(Asia/Seoul)로 설정