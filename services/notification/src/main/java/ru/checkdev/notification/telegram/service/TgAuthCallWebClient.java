package ru.checkdev.notification.telegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.service.EurekaUriProvider;

import javax.annotation.PostConstruct;

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
    @Value("${retry.retries}")
    private int retries;
    @Value("${retry.delay}")
    private long delay;
    private Retry retry;

    @PostConstruct
    public void init() {
        this.retry = new Retry(retries, delay);
    }


    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Override
    public Mono<Profile> doGet(String url) {
        return Mono.fromCallable(() ->
                retry.exec(() ->
                        WebClient.create(uriProvider.getUri(serviceAuthId))
                                .get()
                                .uri(url)
                                .retrieve()
                                .bodyToMono(Profile.class)
                                .block(), null));
    }

    /**
     * Метод POST
     *
     * @param url     URL http
     * @param profile Body PersonDTO.class
     * @return Mono<Person>
     */
    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        return Mono.fromCallable(() ->
                retry.exec(() -> WebClient.create(uriProvider.getUri(serviceAuthId))
                        .post()
                        .uri(url)
                        .bodyValue(profile)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block(), null));
    }

    @Override
    public Mono<Object> doPost(String url) {
        return Mono.fromCallable(() ->
                retry.exec(() -> WebClient.create(uriProvider.getUri(serviceAuthId))
                        .post()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block(), null));
    }
}
