package com.learningpurpose.aichatservice.service;

import reactor.core.publisher.Flux;

public interface AiTutorService {
    Flux<String> streamTutorResponse(String prompt, String subjectContext);
}