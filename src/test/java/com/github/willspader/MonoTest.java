package com.github.willspader;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class MonoTest {

    private Logger logger;

    @Before
    public void setup() {
        logger = LoggerFactory.getLogger(MonoTest.class);
    }

    @Test
    public void shouldPrintSimpleSlf4jInfoMessage() {
        logger.info("Slf4j working");
    }

    @Test
    public void shouldMonoSubscriber() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void shouldMonoSubscriberConsumer() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .log();

        // aplica ações sobre cada um dos valores do publisher
        mono.subscribe(s -> logger.info("Value {}", s));

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void shouldMonoSubscriberConsumerError() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error");
                });

        // aplica ações sobre cada um dos valores do publisher
        mono.subscribe(s -> logger.info("Name {}", s), s -> logger.error("Something bad happened"));
        mono.subscribe(s -> logger.info("Name {}", s), Throwable::printStackTrace);

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

    }

    @Test
    public void shouldMonoSubscriberConsumerComplete() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        // aplica ações sobre cada um dos valores do publisher
        mono.subscribe(s -> logger.info("Value {}", s),
                Throwable::printStackTrace,
                () -> logger.info("finished")
        );

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void shouldMonoSubscriberConsumerSubscription() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        // aplica ações sobre cada um dos valores do publisher
        mono.subscribe(s -> logger.info("Value {}", s),
                Throwable::printStackTrace,
                () -> logger.info("finished"),
                Subscription::cancel
        );

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void shouldMonoDoOnMethods() {
        String name = "William Spader";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> logger.info("Subscribed"))
                .doOnRequest(longNumber -> logger.info("Request Received, starting doing something..."))
                .doOnNext(s -> logger.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> logger.info("doOnSuccess executed"));

        // aplica ações sobre cada um dos valores do publisher
        mono.subscribe(s -> logger.info("Value {}", s),
                Throwable::printStackTrace,
                () -> logger.info("finished")
        );

        logger.info("-----------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

}
