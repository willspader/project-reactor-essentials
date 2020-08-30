package com.github.willspader;

import org.junit.Before;
import org.junit.Test;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

public class FluxTest {

    private Logger logger;

    @Before
    public void setup() {
        logger = LoggerFactory.getLogger(FluxTest.class);
    }

    @Test
    public void shouldFluxSubscriber() {
        Flux<String> fluxString = Flux.just("William", "Cabral", "Donato", "Spader")
                .log();

        StepVerifier.create(fluxString)
                .expectNext("William", "Cabral", "Donato", "Spader")
                .verifyComplete();
    }

    @Test
    public void shouldFluxSubscriberNumbers() {
        Flux<Integer> flux = Flux.range(1, 5)
                .log();

        flux.subscribe(idx -> logger.info("number {}", idx));

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void shouldFluxSubscriberFromList() {
        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .log();

        flux.subscribe(idx -> logger.info("number {}", idx));

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void shouldFluxSubscriberNumbersError() {
        Flux<Integer> flux = Flux.range(1, 5)
                .log()
                .map(i -> {
                    if (i == 4) {
                        throw new IllegalArgumentException("just for learning purposes");
                    }
                    return i;
                });

        flux.subscribe(idx -> logger.info("number {}", idx),
                Throwable::printStackTrace,
                () -> logger.info("DONE"),
                subscription -> subscription.request(3)
        );

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void shouldFluxSubscriberNumbersUglyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        // de 2 em 2
        flux.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void shouldFluxSubscriberNumbersNotSoUglyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        // de 2 em 2
        flux.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void shouldFluxSubscriberNumbersPrettyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        flux.subscribe(idx -> logger.info("number {}", idx));

        logger.info("---------------");
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(idx -> logger.info("number {}", idx));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(24))
                .thenAwait(Duration.ofDays(2))
                .expectNext(0L)
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    @Test
    public void connectableFlux() throws Exception {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100))
                .publish();

        // connectableFlux.connect();

        /*logger.info("Thread sleeping for 300ms");

        // perdeu os dois primeiros numeros
        Thread.sleep(300);

        connectableFlux.subscribe(idx -> logger.info("number {}", idx));

        Thread.sleep(300);

        connectableFlux.subscribe(idx -> logger.info("number {}", idx));*/

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6, 7, 8, 9, 10)
                .expectComplete()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .log();
    }

}
