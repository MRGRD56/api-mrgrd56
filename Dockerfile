FROM ubuntu:22.04 as pre-build-app
WORKDIR /src/
COPY . .
RUN mkdir -p /poms && find . -name pom.xml -exec cp --parents {} /poms \;

FROM ubuntu:22.04 as build-app
WORKDIR /build

RUN apt update && apt install -y openjdk-17-jdk openjdk-17-jre

ADD .mvn ./.mvn
ADD mvnw .

COPY --from=pre-build-app /poms/ ./
RUN chmod a+x ./mvnw && ./mvnw dependency:go-offline dependency:resolve-plugins
ADD . .
RUN chmod a+x ./mvnw && ./mvnw clean package -Dmaven.test.skip

FROM ubuntu:22.04 as app

#RUN apk add cmake g++ wget unzip;

RUN add-apt-repository ppa:alex-p/tesseract-ocr-devel
RUN apt update
RUN apt install -y tesseract-ocr && \
    apt install -y tesseract-ocr-rus tesseract-ocr-jpn tesseract-ocr-chi_sim tesseract-ocr-chi_tra tesseract-ocr-spa tesseract-ocr-hin tesseract-ocr-ben tesseract-ocr-por tesseract-ocr-data-vie tesseract-ocr-deu tesseract-ocr-fra tesseract-ocr-ita tesseract-ocr-bel tesseract-ocr-ukr tesseract-ocr-kor

ENV JAVA_OPTS="-Xms1G -Xmx2G"
COPY --from=build-app /build/target/*.jar /app.jar
CMD ["sh", "-c", "eval exec java -server ${JAVA_OPTS} -jar app.jar"]

#*
#* If you need ICU with non-English locales and legacy charset support, install
#* package icu-data-full.
#*