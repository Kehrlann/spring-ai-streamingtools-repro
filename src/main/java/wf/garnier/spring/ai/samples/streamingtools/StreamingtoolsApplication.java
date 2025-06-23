package wf.garnier.spring.ai.samples.streamingtools;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreamingtoolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamingtoolsApplication.class, args);
    }

    static class WeatherTool {

        @Tool(description = "Get the temperature (in celsius) for a specific location")
        WeatherResponse getTemperature(@ToolParam(description = "The location latitude") double latitude,
                                       @ToolParam(description = "The location longitude") double longitude) {
            return new WeatherResponse();

        }

    }

    public record WeatherResponse(LocalDateTime time, double temperature) {

        public WeatherResponse() {
            this(LocalDateTime.now(), new Random().nextDouble(-5.0, 40.0));
        }
    }


    @Bean
    CommandLineRunner clr(ChatModel chatModel) {
        return args -> {
            var question = "What's the weather like in Paris?";
            System.out.printf("\n\n🤔 Asking the LLM: \"%s\"\n\n", question);
            System.out.println("💭 ... dreaming of electric sheep ...\n");

            var response = getBlockingResponse(chatModel, question);

            System.out.println("🤖 The LLM says ...\n");
            System.out.println(response);
        };
    }

    private static String getBlockingResponse(ChatModel chatModel, String question) {
        return ChatClient.create(chatModel).prompt(question)
                .tools(new WeatherTool())
                .stream()
                .chatResponse()
                .flatMapIterable(ChatResponse::getResults)
                .map(Generation::getOutput)
                .mapNotNull(AbstractMessage::getText)
                .collect(Collectors.joining())
                .block();
    }

    // If you prefer, you can stream the response to STDOUT
    private static void streamResponseToStdout(ChatModel chatModel, String question) {
        ChatClient.create(chatModel).prompt(question)
                .tools(new WeatherTool())
                .stream()
                .chatResponse()
                .flatMapIterable(ChatResponse::getResults)
                .map(Generation::getOutput)
                .mapNotNull(AbstractMessage::getText)
                .doOnNext(System.out::print)
                .blockLast();
    }
}
