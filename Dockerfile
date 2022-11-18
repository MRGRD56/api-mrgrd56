FROM busybox as pre-build-app
WORKDIR /src/
COPY . .
RUN mkdir -p /poms && find . -name pom.xml -exec cp --parents {} /poms \;

FROM maven:3.8.4-openjdk-17-slim as build-app
WORKDIR /build
COPY --from=pre-build-app /poms/ ./
RUN mvn dependency:go-offline dependency:resolve-plugins -B
ADD . .
RUN mvn clean package

FROM openjdk:17-jdk-alpine as app

#RUN add-apt-repository ppa:alex-p/tesseract-ocr-devel
#RUN apt update
RUN apk add tesseract-ocr
RUN apk add tesseract-ocr-data-afr tesseract-ocr-data-ara tesseract-ocr-data-aze tesseract-ocr-data-bel tesseract-ocr-data-ben tesseract-ocr-data-bul tesseract-ocr-data-cat tesseract-ocr-data-ces tesseract-ocr-data-chi_sim tesseract-ocr-data-chi_tra tesseract-ocr-data-chr tesseract-ocr-data-dan tesseract-ocr-data-deu tesseract-ocr-data-ell tesseract-ocr-data-enm tesseract-ocr-data-epo tesseract-ocr-data-equ tesseract-ocr-data-est tesseract-ocr-data-eus tesseract-ocr-data-fin tesseract-ocr-data-fra tesseract-ocr-data-frk tesseract-ocr-data-frm tesseract-ocr-data-glg tesseract-ocr-data-grc tesseract-ocr-data-heb tesseract-ocr-data-hin tesseract-ocr-data-hrv tesseract-ocr-data-hun tesseract-ocr-data-ind tesseract-ocr-data-isl tesseract-ocr-data-ita tesseract-ocr-data-ita_old tesseract-ocr-data-jpn tesseract-ocr-data-kan tesseract-ocr-data-kat tesseract-ocr-data-kor tesseract-ocr-data-lav tesseract-ocr-data-lit tesseract-ocr-data-mal tesseract-ocr-data-mkd tesseract-ocr-data-mlt tesseract-ocr-data-msa tesseract-ocr-data-nld tesseract-ocr-data-nor tesseract-ocr-data-pol tesseract-ocr-data-por tesseract-ocr-data-ron tesseract-ocr-data-rus tesseract-ocr-data-slk tesseract-ocr-data-slv tesseract-ocr-data-spa tesseract-ocr-data-spa_old tesseract-ocr-data-sqi tesseract-ocr-data-srp tesseract-ocr-data-swa tesseract-ocr-data-swe tesseract-ocr-data-tam tesseract-ocr-data-tel tesseract-ocr-data-tgl tesseract-ocr-data-tha tesseract-ocr-data-tur tesseract-ocr-data-ukr tesseract-ocr-data-vie

ENV JAVA_OPTS="-Xms1G -Xmx2G"
COPY --from=build-app /build/target/*.jar /app.jar
CMD ["sh", "-c", "eval exec java -server ${JAVA_OPTS} -jar app.jar"]