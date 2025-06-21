FROM maven:3.9.6 AS BUILD
WORKDIR /usr/app/
COPY ../pom.xml .
COPY pom.xml ./main-app/
COPY src ./main-app/src
RUN mvn -pl main-app -am package

FROM amazoncorretto:21
ENV JAR_NAME=intershop.jar
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/main-app/target/$JAR_NAME $APP_HOME/
EXPOSE 8080
ENTRYPOINT exec java -jar $APP_HOME/$JAR_NAME