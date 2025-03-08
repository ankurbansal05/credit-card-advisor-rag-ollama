package dev.nano.sbot.configuration;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.nano.sbot.retriever.EmbeddingStoreLoggingRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;

import java.time.Duration;
import java.util.List;

import static dev.nano.sbot.constant.Constants.PROMPT_TEMPLATE_2;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class LangChainConfiguration {

    @Value("${langchain.api.key}")
    private String apiKey;

    @Value("${langchain.timeout}")
    private Long timeout;

    private final List<Document> documents;

    @Bean
    public ConversationalRetrievalChain chain() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        log.info("Ingesting Spring Boot Resources ...");

        ingestor.ingest(documents);

        log.info("Ingested {} documents", documents.size());

/*
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();
*/

        /*MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();*/


        EmbeddingStoreRetriever retriever = EmbeddingStoreRetriever.from(embeddingStore, embeddingModel);
        EmbeddingStoreLoggingRetriever loggingRetriever = new EmbeddingStoreLoggingRetriever(retriever);

        log.info("Building ConversationalRetrievalChain ...");
        /*OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-3.5-turbo")  // Now required
                .temperature(0.7)           // Now required
                .timeout(Duration.ofSeconds(timeout))
                .build();*/

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")  // Ollama's default endpoint
                .modelName("llama3.2")                // Specify the Ollama model you want to use
                .temperature(0.7)
                .timeout(Duration.ofSeconds(timeout))
                .build();


/*        DefaultContentInjector dci = DefaultContentInjector.builder()
                .promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE_2))
                .build();

        DefaultRetrievalAugmentor dra = DefaultRetrievalAugmentor.builder()
                .contentInjector(dci)
                .build();*/


        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(chatModel)
                .promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE_2))
                //.chatMemory(chatMemory)
                .retriever(loggingRetriever)
                .build();
        log.info("Spring Boot knowledge base is ready!");

        return chain;
    }
}
