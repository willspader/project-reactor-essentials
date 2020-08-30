package com.github.willspader;

import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class OperatorsTest {

    private Logger logger;

    @Before
    public void setup() {
        logger = LoggerFactory.getLogger(FluxTest.class);
    }

    @Test
    public void subscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(idx -> {
                    logger.info("Map 1 - Number {} on Thread {}", idx, Thread.currentThread().getName());
                    return idx;
                })
                .subscribeOn(Schedulers.single()) // afeta toda a chain
                .map(idx -> {
                    logger.info("Map 2 - Number {} on Thread {}", idx, Thread.currentThread().getName());
                    return idx;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(idx -> {
                    logger.info("Map 1 - Number {} on Thread {}", idx, Thread.currentThread().getName());
                    return idx;
                })
                .publishOn(Schedulers.single()) // afeta apenas abaixo da chain em outra thread
                .map(idx -> {
                    logger.info("Map 2 - Number {} on Thread {}", idx, Thread.currentThread().getName());
                    return idx;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void flatMapOperator() throws Exception {
        Flux<String> flux = Flux.just("w", "s");

        Flux<String> flatten = flux.map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

        StepVerifier.create(flatten)
                .expectSubscription()
                .expectNext("Will", "Spader", "Cabral", "Donato")
                .verifyComplete();
    }

    private Flux<String> findByName(String name) {
        return name.equals("W") ? Flux.just("Will", "Spader") : Flux.just("Cabral", "Donato");
    }

}
