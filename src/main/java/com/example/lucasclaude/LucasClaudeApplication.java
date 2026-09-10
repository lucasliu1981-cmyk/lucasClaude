package com.example.lucasclaude;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.Scanner;

@SpringBootApplication
public class LucasClaudeApplication {

    private final ChatClient.Builder chatClientBuilder;

    public LucasClaudeApplication(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public static void main(String[] args) {
        SpringApplication.run(LucasClaudeApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(DeepSeekChatModel deepSeekChatModel,
                                        ChatMemory chatMemory,
                                        SessionMemoryAdvisor sessionMemorySummarizationAdvisor,
                                        ToolService toolService,
                                        ToolCallbackProvider toolCallbackProvider)
    {
        return args -> {

            //动态修改模型参数
            //对话代理（记忆、advisor大模型对话拦截器、结构化输出...)
            ChatClient chatClient= ChatClient.builder(deepSeekChatModel)
                    //对话记忆，用于保存对话历史，此处是默认的，保存在jvm内存中
                    .defaultAdvisors(
                            //MessageChatMemoryAdvisor.builder(chatMemory).build()
                            sessionMemorySummarizationAdvisor,
                            SimpleLoggerAdvisor.builder().build()
                    )
                    //自定义tools
//                    .defaultTools(toolService)
                    .defaultTools(FileSystemTools.builder().build(),
                            ShellTools.builder().build())
                    //绑定MCPServer到ChatClient中
                    .defaultTools(toolCallbackProvider)
                    .build();


            System.out.println("\n我是lucas Cladue:.\n");
            try(Scanner scanner = new Scanner(System.in)){
                while (true){
                    System.out.println("\n>你： ");
                    String userMessage = scanner.nextLine();
                    System.out.println("\n> AI: ");
                    Flux<String> content = chatClient.prompt()
                            .user(userMessage)
                            //用于保存对话记忆
                            .advisors(advisorSpec -> advisorSpec.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY,"lucas111"))
                            //用于流式输出
                            .stream()
                            .content();

                    content.doOnNext(System.out::print)
                            //阻塞线程
                            .blockLast();
                }
            }
        };
    }
}
