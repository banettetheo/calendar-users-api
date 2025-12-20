package com.calendar.users.domain.ports;

import com.calendar.users.domain.models.BusinessUser;
import reactor.core.publisher.Mono;

public interface UserEventPublisher {

    Mono<Long> publishUserCreatedEvent(BusinessUser businessUser);
}
