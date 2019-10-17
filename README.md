# polypus
Lightweight solution to assert that concurrently executed Java codes should be not throw any exceptions.

## benefits
* Spot and collect thrown exceptions by the individual threads and make the test fail if any thrown.
* Makes possible to write fast (< 1s) concurrent tests.
* Makes sure that the parallel threads should start at the same time as much as possible
  (limited by the thread pool's size)
* Have timeout (we don't want long running tests)
* No need to prepare to care about checked exceptions (e.g.: `InterruptedException`)
* No dependency to external libraries
* Timeout and repetition are optional parameters

## usage

Lets assume we have the following (not thread-safe) class

```java
class NotThreadSafeClass {
    private final List<Integer> list = new ArrayList<>();
    private void add(Integer number) {
        list.add(number);
    }
    private List<Integer> getNumbers() {
        return list.stream().collect(Collectors.toList());
    }
}
```
and we want to test it if the class is thread-safe.
With this solution we can do it with the following way:
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