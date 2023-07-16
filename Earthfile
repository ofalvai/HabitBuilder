# TODO:
# pre-bake Gradle wrapper
# figure out Java 11 vs 17
# 

VERSION 0.7
FROM ubuntu:23.04
WORKDIR /gradle-workdir

setup-jdk11:
    FROM eclipse-temurin:11
    SAVE ARTIFACT /opt/java/openjdk

setup-jdk17:
    FROM eclipse-temurin:17
    SAVE ARTIFACT /opt/java/openjdk
    
setup-android:
    ENV JAVA_HOME_11=/opt/java/openjdk11
    COPY +setup-jdk11/openjdk $JAVA_HOME_11

    ENV JAVA_HOME_17=/opt/java/openjdk17
    COPY +setup-jdk17/openjdk $JAVA_HOME_17

    ENV JAVA_HOME $JAVA_HOME_17

    ENV ANDROID_HOME "/home/ci/android-sdk"
    ENV ANDROID_SDK_ROOT $ANDROID_HOME
    ENV CMDLINE_TOOLS_ROOT "${ANDROID_HOME}/cmdline-tools/latest/bin"
    ENV ADB_INSTALL_TIMEOUT 120
    ENV PATH "${ANDROID_HOME}/emulator:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${PATH}"

    RUN apt-get update && apt-get install -y unzip curl

    RUN SDK_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip" && \
        mkdir -p ${ANDROID_HOME}/cmdline-tools && \
        mkdir ${ANDROID_HOME}/platforms && \
        mkdir ${ANDROID_HOME}/ndk && \
        curl -L -o /tmp/cmdline-tools.zip "${SDK_TOOLS_URL}" && \
        unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
        rm /tmp/cmdline-tools.zip && \
        mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest
    

    COPY fake_emulator_package.xml /tmp/package.xml
    RUN FAKE_EMULATOR_URL="https://redirector.gvt1.com/edgedl/android/repository/emulator-linux_x64-9536276.zip" && \
         mkdir -p ${ANDROID_HOME}/emulator && \
         curl -L -o /tmp/fake-emulator.zip "${FAKE_EMULATOR_URL}" && \
         unzip -q /tmp/fake-emulator.zip -d ${ANDROID_HOME}/emulator && \
         rm /tmp/fake-emulator.zip && \
         mv /tmp/package.xml ${ANDROID_HOME}/emulator/package.xml

    RUN echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "tools" && \
        echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "platform-tools" && \
        echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "build-tools;33.0.1"
    
    RUN echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "platforms;android-33"

deps:
    # Run a Gradle build with the bare minimum files so that this Docker layer can be cached
    # and not get invalidated when project files change

    FROM +setup-android

    COPY gradle.properties ./gradle.properties
    COPY gradle/wrapper ./gradle/wrapper
    COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
    COPY build.gradle ./build.gradle
    COPY gradlew ./gradlew
    COPY lint.xml ./lint.xml
    COPY settings.gradle ./settings.gradle
    COPY build-logic ./build-logic

    RUN ./gradlew --no-daemon

build:
    FROM +deps

    COPY app ./app
    COPY core-common ./core-common
    COPY core-database ./core-database
    COPY core-model ./core-model
    COPY core-testing ./core-testing
    COPY core-ui ./core-ui
    COPY feature-dashboard ./feature-dashboard
    COPY feature-insights ./feature-insights
    COPY feature-misc ./feature-misc
    COPY feature-widgets ./feature-widgets

    RUN ./gradlew assembleDebug --no-daemon
    SAVE ARTIFACT app/build/outputs/apk
