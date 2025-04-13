FROM maven:3.9.6 AS BUILD
WORKDIR /usr/app/
COPY pom.xml .
COPY src ./src
RUN mvn package

# Package stage
FROM amazoncorretto:21
ENV JAR_NAME=intershop.jar
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/target/$JAR_NAME $APP_HOME/
EXPOSE 8080
ENTRYPOINT exec java -jar $APP_HOME/$JAR_NAME