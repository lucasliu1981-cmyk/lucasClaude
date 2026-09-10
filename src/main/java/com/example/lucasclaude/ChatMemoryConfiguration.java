package com.example.lucasclaude;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.RecursiveSummarizationCompactionStrategy;
import org.springframework.ai.session.compaction.SlidingWindowCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ChatMemoryConfiguration {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(2)
                .chatMemoryRepository(chatMemoryRepository).build();
    }

    @Bean
    SessionMemoryAdvisor sessionMemoryAdvisor() {
        SessionService service =
                DefaultSessionService.builder().sessionRepository(
                        InMemorySessionRepository.builder().build()
                ).build();

        return SessionMemoryAdvisor.builder(service)
                .defaultUserId("lucas")
                //触发器，超过1轮就触发对话压缩
                .compactionTrigger(new TurnCountTrigger(1))
                //压缩策略，滑动窗口，最多保存1个事件
                .compactionStrategy(SlidingWindowCompactionStrategy.builder().maxEvents(1).build())
                .build();
    }
    @Bean
    SessionMemoryAdvisor sessionMemorySummarizationAdvisor(DeepSeekChatModel deepSeekChatModel) {
        SessionService service =
                DefaultSessionService.builder().sessionRepository(
                        InMemorySessionRepository.builder().build()
                ).build();

        return SessionMemoryAdvisor.builder(service)
                .defaultUserId("lucas")
                //触发器，超过1轮就触发对话压缩
                .compactionTrigger(new TurnCountTrigger(1))
                //压缩策略，滑动窗口，最多保存1个事件
                .compactionStrategy(
                        RecursiveSummarizationCompactionStrategy
                                .builder(
                                    ChatClient.builder(deepSeekChatModel).build()
                                )
                                //保留不压缩条数
                                .maxEventsToKeep(2)
                                //除压缩外，额外提供给LLM的条数
                                .overlapSize(1)
                        .build()
                )
                .build();
    }
}
