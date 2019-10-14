package com.kanbagoly.polypus;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.kanbagoly.polypus.ConcurrentAssertions.assertConcurrently;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConcurrentAssertionsTest {

    @Test
    void notThreadSafeExecutionShouldThrow() {
        NotThreadSafeClass notThreadSafe = new NotThreadSafeClass();
        Runnable creator = () -> notThreadSafe.add(1);
        Runnable query = () -> notThreadSafe.getNumbers();

        ThrowableAssert.ThrowingCallable testShouldFail = () ->
                assertConcurrently(creator, query)
                        .repeatedCalls(100)
                        .timeoutAfter(1, TimeUnit.SECONDS)
                        .shouldNotThrow();

        assertThatCode(testShouldFail)
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith(
                        "Test failed with the following exception(s): java.util.ConcurrentModificationException");
    }

    private static class NotThreadSafeClass {
        private List<Integer> list = new ArrayList<>();
        private void add(Integer number) {
            list.add(number);
        }
        private List<Integer> getNumbers() {
            return list.stream().collect(Collectors.toList());
        }
    }

}
