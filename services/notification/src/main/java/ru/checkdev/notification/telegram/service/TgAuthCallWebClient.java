package ru.checkdev.notification.telegram.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.service.EurekaUriProvider;

/**
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@org.springframework.context.annotation.Profile("default")
@Service
@RequiredArgsConstructor
@Slf4j
public class TgAuthCallWebClient implements TgCall {
    @Value("${server.auth}")
    private String serviceAuthId;
    private final EurekaUriProvider uriProvider;


    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Retry(name = "tgAuthRetry")
    @CircuitBreaker(name = "tgAuthCircuitBreaker", fallbackMethod = "fallbackGet") // Применение Circuit Breaker
    @Override
    public Mono<Profile> doGet(String url) {
        return WebClient.create(uriProvider.getUri(serviceAuthId))
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Profile.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    /**
     * Метод POST
     *
     * @param url     URL http
     * @param profile Body PersonDTO.class
     * @return Mono<Person>
     */
    @Retry(name = "tgAuthRetry") // Применение Retry
    @CircuitBreaker(name = "tgAuthCircuitBreaker", fallbackMethod = "fallbackPost")
    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        return WebClient.create(uriProvider.getUri(serviceAuthId))
                .post()
                .uri(url)
                .bodyValue(profile)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    @Retry(name = "tgAuthRetry") // Применение Retry
    @CircuitBreaker(name = "tgAuthCircuitBreaker", fallbackMethod = "fallbackPost")
    @Override
    public Mono<Object> doPost(String url) {
        return WebClient.create(uriProvider.getUri(serviceAuthId))
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    public Mono<Profile> fallbackGet(String url, Throwable throwable) {
        log.error("GET request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty();
    }

    public Mono<Object> fallbackPost(String url, Profile profile, Throwable throwable) {
        log.error("POST request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty();
    }
}
