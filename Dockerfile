# ============================================================
#  AuraPlay – Docker-based APK builder
#  Usage:  docker build -t auraplay-builder .
#          docker run --rm -v $(pwd)/output:/output auraplay-builder
# ============================================================
FROM eclipse-temurin:17-jdk-jammy

# System deps
RUN apt-get update && apt-get install -y --no-install-recommends \
        unzip wget curl git \
    && rm -rf /var/lib/apt/lists/*

# Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    cd /tmp && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdtools.zip && \
    unzip -q cmdtools.zip && \
    mv cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm cmdtools.zip

RUN yes | sdkmanager --licenses > /dev/null 2>&1 && \
    sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# Copy project
WORKDIR /app
COPY . .

# Make wrapper executable
RUN chmod +x gradlew 2>/dev/null || true

# Build
RUN ./gradlew assembleDebug --no-daemon || gradle assembleDebug --no-daemon

# Copy APK out
RUN mkdir -p /output && \
    cp app/build/outputs/apk/debug/*.apk /output/AuraPlay-debug.apk 2>/dev/null || true

VOLUME /output
CMD ["echo", "Build complete. APK is in /output/"]
