FROM amazoncorretto:17-alpine as pre-build-app
WORKDIR /src/
COPY . .
RUN mkdir -p /poms && find . -name pom.xml -exec cp --parents {} /poms \;

FROM amazoncorretto:17-alpine as build-app
WORKDIR /build

ADD .mvn ./.mvn
ADD mvnw .
RUN chmod a+x ./mvnw

COPY --from=pre-build-app /poms/ ./
RUN ./mvnw -B -e -C -T 1C -npu -llr dependency:go-offline dependency:resolve-plugins
ADD . .
RUN ./mvnw clean package

FROM amazoncorretto:17-alpine as app

#RUN apk add cmake g++ wget unzip;

#RUN add-apt-repository ppa:alex-p/tesseract-ocr-devel
#RUN apt update
RUN apk add -X https://dl-cdn.alpinelinux.org/alpine/v3.17/main -u alpine-keys
RUN apk update
RUN apk add -X https://dl-cdn.alpinelinux.org/alpine/v3.17/main gcompat opencv
RUN apk add -X https://dl-cdn.alpinelinux.org/alpine/v3.17/community -X https://dl-cdn.alpinelinux.org/alpine/v3.17/main tesseract-ocr=5.2.0-r1
RUN apk add -X https://dl-cdn.alpinelinux.org/alpine/v3.17/community -X https://dl-cdn.alpinelinux.org/alpine/v3.17/main tesseract-ocr-data-rus=5.2.0-r1 tesseract-ocr-data-jpn=5.2.0-r1 tesseract-ocr-data-chi_sim=5.2.0-r1 tesseract-ocr-data-chi_tra=5.2.0-r1 tesseract-ocr-data-spa=5.2.0-r1 tesseract-ocr-data-hin=5.2.0-r1 tesseract-ocr-data-ben=5.2.0-r1 tesseract-ocr-data-por=5.2.0-r1 tesseract-ocr-data-vie tesseract-ocr-data-deu=5.2.0-r1 tesseract-ocr-data-fra=5.2.0-r1 tesseract-ocr-data-ita=5.2.0-r1 tesseract-ocr-data-bel=5.2.0-r1 tesseract-ocr-data-ukr=5.2.0-r1 tesseract-ocr-data-kor=5.2.0-r1
#RUN apk add -X https://dl-cdn.alpinelinux.org/alpine/v3.17/community -X https://dl-cdn.alpinelinux.org/alpine/v3.17/main tesseract-ocr-data-afr=5.2.0-r1 tesseract-ocr-data-ara=5.2.0-r1 tesseract-ocr-data-aze=5.2.0-r1 tesseract-ocr-data-bel=5.2.0-r1 tesseract-ocr-data-ben=5.2.0-r1 tesseract-ocr-data-bul=5.2.0-r1 tesseract-ocr-data-cat=5.2.0-r1 tesseract-ocr-data-ces=5.2.0-r1 tesseract-ocr-data-chi_sim=5.2.0-r1 tesseract-ocr-data-chi_tra=5.2.0-r1 tesseract-ocr-data-chr=5.2.0-r1 tesseract-ocr-data-dan=5.2.0-r1 tesseract-ocr-data-deu=5.2.0-r1 tesseract-ocr-data-ell=5.2.0-r1 tesseract-ocr-data-enm=5.2.0-r1 tesseract-ocr-data-epo=5.2.0-r1 tesseract-ocr-data-equ=5.2.0-r1 tesseract-ocr-data-est=5.2.0-r1 tesseract-ocr-data-eus=5.2.0-r1 tesseract-ocr-data-fin=5.2.0-r1 tesseract-ocr-data-fra=5.2.0-r1 tesseract-ocr-data-frk=5.2.0-r1 tesseract-ocr-data-frm=5.2.0-r1 tesseract-ocr-data-glg=5.2.0-r1 tesseract-ocr-data-grc=5.2.0-r1 tesseract-ocr-data-heb=5.2.0-r1 tesseract-ocr-data-hin=5.2.0-r1 tesseract-ocr-data-hrv=5.2.0-r1 tesseract-ocr-data-hun=5.2.0-r1 tesseract-ocr-data-ind=5.2.0-r1 tesseract-ocr-data-isl=5.2.0-r1 tesseract-ocr-data-ita=5.2.0-r1 tesseract-ocr-data-ita_old=5.2.0-r1 tesseract-ocr-data-jpn=5.2.0-r1 tesseract-ocr-data-kan=5.2.0-r1 tesseract-ocr-data-kat=5.2.0-r1 tesseract-ocr-data-kor=5.2.0-r1 tesseract-ocr-data-lav=5.2.0-r1 tesseract-ocr-data-lit=5.2.0-r1 tesseract-ocr-data-mal=5.2.0-r1 tesseract-ocr-data-mkd=5.2.0-r1 tesseract-ocr-data-mlt=5.2.0-r1 tesseract-ocr-data-msa=5.2.0-r1 tesseract-ocr-data-nld=5.2.0-r1 tesseract-ocr-data-nor=5.2.0-r1 tesseract-ocr-data-pol=5.2.0-r1 tesseract-ocr-data-por=5.2.0-r1 tesseract-ocr-data-ron=5.2.0-r1 tesseract-ocr-data-rus=5.2.0-r1 tesseract-ocr-data-slk=5.2.0-r1 tesseract-ocr-data-slv=5.2.0-r1 tesseract-ocr-data-spa=5.2.0-r1 tesseract-ocr-data-spa_old=5.2.0-r1 tesseract-ocr-data-sqi=5.2.0-r1 tesseract-ocr-data-srp=5.2.0-r1 tesseract-ocr-data-swa=5.2.0-r1 tesseract-ocr-data-swe=5.2.0-r1 tesseract-ocr-data-tam=5.2.0-r1 tesseract-ocr-data-tel=5.2.0-r1 tesseract-ocr-data-tgl=5.2.0-r1 tesseract-ocr-data-tha=5.2.0-r1 tesseract-ocr-data-tur=5.2.0-r1 tesseract-ocr-data-ukr=5.2.0-r1 tesseract-ocr-data-vie=5.2.0-r1

ENV JAVA_OPTS="-Xms1G -Xmx2G"
COPY --from=build-app /build/target/*.jar /app.jar
CMD ["sh", "-c", "eval exec java -server ${JAVA_OPTS} -jar app.jar"]

#*
#* If you need ICU with non-English locales and legacy charset support, install
#* package icu-data-full.
#*