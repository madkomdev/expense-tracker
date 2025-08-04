FROM alpine/java:21-jdk as builder
LABEL authors="madhu.kommula"

WORKDIR /application

RUN adduser -D app && chown -R app /application

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","/application.jar"]
RUN java -Djarmode=layertools -jar application.jar extract

FROM builder

WORKDIR /application
COPY --from=builder application/dependencies/BOOT-INF/lib ./lib
COPY --from=builder application/application/META-INF ./
COPY --from=builder application/application/BOOT-INF/classes ./

EXPOSE 9000 9001
ENV APP_LOGGER=CONSOLE_JSON

USER app
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=80", "-cp", ".:./lib/*", "com.expense.tracker.ExpenseTrackerApplicationKt"]
