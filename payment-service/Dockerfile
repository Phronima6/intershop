FROM maven:3.9.6 AS BUILD
WORKDIR /usr/app/
COPY ../pom.xml .
COPY pom.xml ./payment-service/
COPY src ./payment-service/src
RUN mvn -pl payment-service -am package

FROM amazoncorretto:21
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/payment-service/target/payment-service-1.0-SNAPSHOT.jar $APP_HOME/app.jar
EXPOSE 8081
ENTRYPOINT exec java -jar $APP_HOME/app.jar