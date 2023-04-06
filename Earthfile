# TODO:
# pre-bake Gradle wrapper
# 

VERSION 0.7
FROM eclipse-temurin:17
WORKDIR /gradle-workdir

setup-jdk11:
    FROM eclipse-temurin:11
    SAVE ARTIFACT /opt/java/openjdk

setup-jdk17:
    FROM eclipse-temurin:17
    SAVE ARTIFACT /opt/java/openjdk
    
setup-android:
    ENV ANDROID_HOME "/home/circleci/android-sdk"
    ENV ANDROID_SDK_ROOT $ANDROID_HOME
    ENV CMDLINE_TOOLS_ROOT "${ANDROID_HOME}/cmdline-tools/latest/bin"
    ENV ADB_INSTALL_TIMEOUT 120
    ENV PATH "${ANDROID_HOME}/emulator:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${PATH}"

    RUN apt-get update && apt-get install unzip

    RUN SDK_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-mac-9477386_latest.zip" && \
        mkdir -p ${ANDROID_HOME}/cmdline-tools && \
        mkdir ${ANDROID_HOME}/platforms && \
        mkdir ${ANDROID_HOME}/ndk && \
        wget -O /tmp/cmdline-tools.zip -t 5 "${SDK_TOOLS_URL}" && \
        unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
        rm /tmp/cmdline-tools.zip && \
        mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest
    

    COPY fake_emulator_package.xml /tmp/package.xml
    RUN FAKE_EMULATOR_URL="https://redirector.gvt1.com/edgedl/android/repository/emulator-linux_x64-9536276.zip" && \
         mkdir -p ${ANDROID_HOME}/emulator && \
         wget -O /tmp/fake-emulator.zip -t 5 "${FAKE_EMULATOR_URL}" && \
         unzip -q /tmp/fake-emulator.zip -d ${ANDROID_HOME}/emulator && \
         rm /tmp/fake-emulator.zip && \
         mv /tmp/package.xml ${ANDROID_HOME}/emulator/package.xml

    RUN echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "tools" && \
        echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "platform-tools" && \
        echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "build-tools;33.0.1"
    
    RUN echo y | ${CMDLINE_TOOLS_ROOT}/sdkmanager "platforms;android-33"

deps:
    FROM +setup-android

    ENV JAVA_HOME_11=/opt/java/openjdk11
    COPY +setup-jdk11/openjdk $JAVA_HOME_11

    ENV JAVA_HOME_17=/opt/java/openjdk17
    COPY +setup-jdk17/openjdk $JAVA_HOME_17

    COPY gradle.properties ./gradle.properties
    COPY gradle/wrapper ./gradle/wrapper
    COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
    COPY build.gradle ./build.gradle
    COPY gradlew ./gradlew
    COPY lint.xml ./lint.xml
    COPY settings.gradle ./settings.gradle

    COPY build-logic ./build-logic


    # RUN ./gradlew -q javaToolchains || true

    RUN --mount=type=cache,target=/root/.gradle/caches \
        --mount=type=cache,target=/root/.gradle/wrapper \
        ./gradlew

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

    RUN ./gradlew assembleDebug
    SAVE ARTIFACT app/build/outputs/apk