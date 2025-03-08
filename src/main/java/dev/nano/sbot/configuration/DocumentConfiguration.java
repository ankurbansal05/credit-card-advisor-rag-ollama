package dev.nano.sbot.configuration;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;

import static dev.nano.sbot.constant.Constants.SPRING_BOOT_RESOURCES_LIST;

@Configuration
public class DocumentConfiguration {
    @Bean
    public List<Document> documents() {
        return SPRING_BOOT_RESOURCES_LIST.stream()
                .map(urlString -> {
                    try {
                        URL url = new URL(urlString);
                        return UrlDocumentLoader.load(url, new TextDocumentParser());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load document from " + urlString, e);
                    }
                })
                .toList();
    }
}
