# 🐙 polypus
Lightweight solution to assert that concurrently executed Java codes should not throw any exceptions.

## benefits
* Report thrown exceptions by the individual threads and fail the test if any thrown
* Possible to write fast (< 1s) concurrency tests
* Make sure that the parallel threads start at the same time as much as possible
  (limited by the thread pool's size)
* Test have timeout (we don't want long running tests)
* No need to care about checked exceptions (e.g.: `InterruptedException`) in the method signatures
* No dependency on external libraries
* The timeout and repetition parameters are optional to support simpler cases 

## usage

Let's assume we have the following (not thread-safe) class
```java
class NotThreadSafeClass {
    private final List<Integer> numbers = new ArrayList<>();
    private void add(Integer number) {
        numbers.add(number);
    }
    private List<Integer> getNumbers() {
        return numbers.stream().collect(Collectors.toList());
    }
}
```
and we want to test if the class is thread-safe.
With this solution we can assert that the parallel execution of the class' methods should not throw any exceptions:
```java
@Test
void parallelExecutionShouldNotThrow() {
    NotThreadSafeClass notThreadSafe = new NotThreadSafeClass();
    Runnable creator = () -> notThreadSafe.add(1);
    Runnable query = () -> notThreadSafe.getNumbers();

    assertConcurrently(creator, query)
            .repeatedCalls(100)
            .timeoutAfter(1, TimeUnit.SECONDS)
            .shouldNotThrow();
}
```