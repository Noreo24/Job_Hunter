# Stage 1: Build the application
FROM gradle:8.7-jdk17 AS build
COPY --chown=gradle:gradle . /noreo/jobhunter
WORKDIR /noreo/jobhunter

#skip task: test
RUN gradle clean build -x test --no-daemon

# Stage 2: Run the application
FROM openjdk:17-slim
EXPOSE 8080
COPY --from=build /noreo/jobhunter/build/libs/*.jar /noreo/jobhunter-BE-spring.jar
ENTRYPOINT ["java", "-jar", "/noreo/jobhunter-BE-spring.jar"]
