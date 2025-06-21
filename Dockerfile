FROM maven:3.9.6 AS build
WORKDIR /usr/app/
COPY pom.xml .
COPY main-app/pom.xml main-app/
COPY payment-service/pom.xml payment-service/

RUN mvn dependency:go-offline -B

COPY main-app/src/ main-app/src/
COPY payment-service/src/ payment-service/src/
RUN mvn clean package -DskipTests

FROM amazoncorretto:21 AS main-app
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
RUN yum install -y curl
COPY --from=build /usr/app/main-app/target/intershop.jar $APP_HOME/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM amazoncorretto:21 AS payment-service
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
RUN yum install -y curl
COPY --from=build /usr/app/payment-service/target/payment-service-1.0-SNAPSHOT.jar $APP_HOME/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"] 