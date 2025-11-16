# PC001 — Producer/Consumer

This repository is a compact demonstration of the classic Producer-Consumer pattern in Java. It includes two buffer implementations, a small observer/memento subsystem, unit tests, and a standalone test suite that prints results to the console.

Key points
- Two buffer implementations:
  - `SharedBuffer` — `ArrayBlockingQueue`-backed (recommended for production-like use)
  - `SharedBufferWaitNotify` — `synchronized` + `wait()`/`notifyAll()` demo to show low-level monitor coordination
- A simple `Manager`/`QueueObserver` system for optional per-operation logging and snapshot (memento) capture
- JUnit 5 tests (run with Maven) and a standalone `TestSharedBufferSuite` (runs without JUnit and prints a clear console summary)

Prerequisites
- Java 11+ (project `pom.xml` is set to compile for Java 11)
- Maven (for running unit tests with JUnit)

Project layout (selected files)
- `src/main/java/pc001/buffer/`
  - `SharedBufferInterface.java` — buffer contract (put/take/snapshot/metrics)
  - `SharedBuffer.java` — BlockingQueue implementation
  - `SharedBufferWaitNotify.java` — wait/notify implementation
- `src/main/java/pc001/core/`
  - `Producer.java`, `Consumer.java` — runnables used by harness
  - `MainPC001.java` — main harness (supports `blocking` and `wait` modes and a `-v` verbose flag)
- `src/main/java/pc001/observer/` and `src/main/java/pc001/memento/` — optional monitoring/snapshot utilities
- `src/test/java/pc001/TestSharedBufferJUnit.java` — JUnit 5 tests (run via `mvn test`)
- `src/test/java/pc001/TestSharedBufferSuite.java` — standalone test suite (run with `java -cp ... pc001.TestSharedBufferSuite`)

How to build
From the project root:

```bash
mvn -q -DskipTests=false test
```

This runs the JUnit tests and reports results via Maven/Surefire. The project is configured to use Java 11 (see `pom.xml` properties `maven.compiler.source`/`target`).

How to run the standalone suite (console output)

After building, run the standalone `TestSharedBufferSuite` (this prints a human-readable per-test summary and exits with code 0 on success, non-zero on failure — suitable for CI):

```bash
cd /Users/sivaspc/Documents/My Projects/Intuit/PC001
java -cp target/test-classes:target/classes:. pc001.TestSharedBufferSuite
```

Sample console output (captured from a local run):

```
Running test for SharedBuffer (producers=2 consumers=2)
SharedBuffer => produced=50 consumed=50 result=PASS
Running test for SharedBufferWaitNotify (producers=2 consumers=2)
SharedBufferWaitNotify => produced=50 consumed=50 result=PASS
Running emptyBufferTest
emptyBufferTest => PASS
Running fullBufferBlockingTest
fullBufferBlockingTest => PASS
Running interruptedProducerTest
interruptedProducerTest => PASS
Running emptyBufferTest
emptyBufferTest => PASS
Running fullBufferBlockingTest
fullBufferBlockingTest => PASS
Running interruptedProducerTest
interruptedProducerTest => PASS
ALL TESTS PASSED
```

How to publish to GitHub (commands you can run locally)

```bash
cd "/Users/sivaspc/Documents/My Projects/Intuit/PC001"
git init
git add .
git commit -m "PC001: producer-consumer demo with BlockingQueue and Wait/Notify implementations"
# Create repo on GitHub via web UI or use `gh` (GitHub CLI) if installed and authenticated:
# gh repo create <username>/pc001 --public --source=. --push
# Or add a remote manually:
# git remote add origin https://github.com/<your-username>/<repo>.git
# git push -u origin main
```

-- end of README
