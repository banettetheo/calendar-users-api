package com.calendar.users.infrastructure.adapters;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.ports.UserEventPublisher;
import com.calendar.users.infrastructure.mappers.KafkaDataMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
public class KafkaUserEventAdapter implements UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaDataMapper kafkaDataMapper;
    private final ObjectMapper objectMapper;

    private static final String USER_CREATED_TOPIC = "USER_CREATED";

    public KafkaUserEventAdapter(KafkaTemplate<String, String> kafkaTemplate, KafkaDataMapper kafkaDataMapper, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaDataMapper = kafkaDataMapper;
        this.objectMapper = objectMapper;
    }

    public Mono<Long> publishUserCreatedEvent(BusinessUser businessUser) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(kafkaDataMapper.toUserCreatedEventDTO(businessUser)))
                .flatMap(payload -> {
                    var future = kafkaTemplate.send(USER_CREATED_TOPIC, String.valueOf(businessUser.id()), payload);
                    return Mono.fromFuture(future);
                })
                .onErrorMap(throwable -> new RuntimeException("Error publishing user created event", throwable))
                .thenReturn(businessUser.id());
    }
}
