package ru.mrgrd56.api.translation.translator

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.stream.Collectors
import java.util.stream.StreamSupport

@Component
class GoogleTranslator : Translator {
    override val name: String
        get() = "google"

    override fun translate(text: String?, from: String?, to: String?): String? {
        val requestHeaders = HttpHeaders().apply {
            this["User-Agent"] = USER_AGENT
        }

        val url = UriComponentsBuilder.fromHttpUrl("https://translate.googleapis.com/translate_a/single")
            .queryParam("client", "gtx")
            .queryParam("dt", "t")
            .queryParam("sl", "{from}")
            .queryParam("tl", "{to}")
            .queryParam("q", "{text}")
            .encode()
            .toUriString()

        val response = RestTemplate().exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Any>(requestHeaders),
            JsonNode::class.java,
            mapOf(
                "from" to from,
                "to" to to,
                "text" to text
            )
        ).body

        return response?.let {
            StreamSupport.stream(it.elements().next().spliterator(), false)
                .map { node: JsonNode -> node.elements().next().asText() }
                .collect(Collectors.joining(""))
        }
    }
    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
    }
}
