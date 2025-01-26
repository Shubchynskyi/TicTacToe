FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

RUN apt-get update && apt-get install -y \
    wget unzip xvfb libxrender1 libxext6 libxi6 libxrandr2 libxtst6 \
    libpangocairo-1.0-0 fonts-liberation libnss3 libatk1.0-0 libatk-bridge2.0-0 libgtk-3-0 \
    && wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && dpkg -i google-chrome-stable_current_amd64.deb || apt-get -fy install \
    && rm google-chrome-stable_current_amd64.deb \
    && wget -q "https://chromedriver.storage.googleapis.com/$(curl -s https://chromedriver.storage.googleapis.com/LATEST_RELEASE)/chromedriver_linux64.zip" \
    && unzip chromedriver_linux64.zip -d /usr/local/bin \
    && rm chromedriver_linux64.zip

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
CMD ["mvn", "verify"]
