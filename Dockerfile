FROM node:21-alpine3.17 as install-npm
WORKDIR /src/main/resources/static
COPY src/main/resources/static/package.json .
COPY src/main/resources/static/package-lock.json .
RUN npm install

FROM ubuntu:22.04 as pre-build-app
WORKDIR /src/
COPY . .
RUN mkdir -p /poms && find . -name pom.xml -exec cp --parents {} /poms \;

FROM maven:3.8.4-openjdk-17-slim as build-app
WORKDIR /build
COPY --from=pre-build-app /poms/ ./
COPY --from=install-npm /src/main/resources/static ./src/main/resources/static
RUN mvn dependency:go-offline dependency:resolve-plugins -B
ADD . .
RUN mvn clean package

FROM ubuntu:22.04 as app

RUN apt update && apt install -y openjdk-17-jdk openjdk-17-jre
RUN apt install -y software-properties-common
RUN add-apt-repository ppa:alex-p/tesseract-ocr-devel
RUN apt update
RUN apt install -y tesseract-ocr && \
    apt install -y tesseract-ocr-rus tesseract-ocr-jpn tesseract-ocr-chi-sim tesseract-ocr-chi-tra tesseract-ocr-spa tesseract-ocr-hin tesseract-ocr-ben tesseract-ocr-por tesseract-ocr-vie tesseract-ocr-deu tesseract-ocr-fra tesseract-ocr-ita tesseract-ocr-bel tesseract-ocr-ukr tesseract-ocr-kor
RUN ln -s /usr/share/tesseract-ocr/5/tessdata /usr/share/tessdata

ENV JAVA_OPTS="-Xms1G -Xmx2G"
COPY --from=build-app /build/target/*.jar /app.jar
CMD ["sh", "-c", "eval exec java -server ${JAVA_OPTS} -jar app.jar"]
