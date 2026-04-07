package com.generixgroup.gnxaitraining.core.service;

import lombok.*;

@Builder
public record ClientSearchCriteria(
    String firstName, String lastName, String email, String phoneNumber) {}
