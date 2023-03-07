package ru.mrgrd56.api.translation.translator;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class GoogleTranslator implements Translator {
    @Override
    public String getName() {
        return "google";
    }

    @Override
    public String translate(String text, String from, String to) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");

        String url = UriComponentsBuilder.fromHttpUrl("https://translate.googleapis.com/translate_a/single")
                .queryParam("client", "gtx")
                .queryParam("dt", "t")
                .queryParam("sl", "{from}")
                .queryParam("tl", "{to}")
                .queryParam("q", "{text}")
                .encode()
                .toUriString();

        var response = new RestTemplate().exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requestHeaders),
                JsonNode.class,
                Map.ofEntries(
                        Map.entry("from", from),
                        Map.entry("to", to),
                        Map.entry("text", text)
                )).getBody();

        return StreamSupport.stream(response.elements().next().spliterator(), false)
                .map(node -> node.elements().next().asText())
                .collect(Collectors.joining(""));
    }
}
