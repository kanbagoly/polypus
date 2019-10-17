# 🐙 polypus
Lightweight solution to assert that concurrently executed Java codes should be not throw any exceptions.

## benefits
* Spot and collect thrown exceptions by the individual threads and make the test fail if any thrown
* Makes possible to write fast (< 1s) concurrency tests
* Makes sure that the parallel threads should start at the same time as much as possible
  (limited by the thread pool's size)
* Test have timeout (we don't want long running tests)
* No need to care about checked exceptions (e.g.: `InterruptedException`) in the method signatures
* No dependency to external libraries
* For simpler usage the timeout and repetition are optional parameters

## usage

Lets assume we have the following (not thread-safe) class

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
and we want to test it if the class is thread-safe.
With this solution we can assert that the parallel execution of the class' methods should not throw any exceptions:
```java
@Test
void notThreadSafeExecutionShouldThrow() {
    NotThreadSafeClass notThreadSafe = new NotThreadSafeClass();
    Runnable creator = () -> notThreadSafe.add(1);
    Runnable query = () -> notThreadSafe.getNumbers();

    assertConcurrently(creator, query)
            .repeatedCalls(100)
            .timeoutAfter(1, TimeUnit.SECONDS)
            .shouldNotThrow();
}
```