package com.calendar.users.domain.ports;

import com.calendar.users.domain.models.BusinessUser;

public interface UserRepositoryPort {

    BusinessUser save(BusinessUser businessUser, String keycloakId);

    BusinessUser getBusinessUserByKeycloakId(String keycloakId);
}
