# polypus
Lightweight solution to assert that concurrently executed Java codes should be not throw any exceptions.

##benefits
* Spot and collect thrown exceptions by the individual threads and make the test's thread fail if any thrown.
* Makes possible to write fast (< 1s) concurrent tests.
* Makes sure that the parallel threads should start at the same time as much as possible (limited by the thread pool's size)
* Have timeout (we don't want long running tests)
* No need to prepare to care about checked exceptions (e.g.: `InterruptedException`)
* Solution has no external dependency
* Timeouts and repetition are optional parameters

##usage