# StudentDirectory — Lab 7: HashMap and Key–Value Structures

A beginner-readable Java 17+ project for the Lab 7 individual technical
activity. It implements a keyed student directory and a character-frequency
counter, demonstrates the four workbook lab activities, and measures HashMap
behaviour under good and deliberately bad hashing.

> **Academic-integrity note:** this project was built with AI assistance. The
> student selected the duplicate-key checkpoint behaviour and must be able to
> explain and modify every submitted line. The exact division of work is in
> [`AGENT_LOG.md`](AGENT_LOG.md). Nothing was published to GitHub.

## Learning objectives

- Store and retrieve values by custom keys with `HashMap`.
- Perform `put`, `get`, `containsKey`, update, remove, and `entrySet()`
  iteration.
- Protect an invariant: student IDs are unique and a duplicate cannot overwrite
  an existing record.
- Return detached, unmodifiable snapshots instead of the mutable backing map.
- Count characters with `HashMap<Character, Integer>` and state the counting
  rules explicitly.
- Connect O(1) average behaviour, collision chains, and O(n) worst-case
  behaviour to actual measurements.
- Separate auxiliary space from running time.

## Features

- **`Student`** — immutable record with validated ID, name, and grade
  (`0..100`).
- **`StudentDirectory`** — `HashMap<String, Student>` with:
  - duplicate-rejecting `addStudent`;
  - `getStudent`, `containsStudent`, `size`;
  - `updateStudent` (never inserts) and `updateGrade` (grade only);
  - `removeStudent`;
  - detached unmodifiable `snapshot()` and `snapshotSortedById()`.
- **`CharacterFrequencyCounter`** — `HashMap<Character, Integer>` counter with an
  explicit, case-sensitive, no-filtering rule.
- **`ProductInventory`** — Task 4 product code → quantity application.
- **`LibraryBookTracker`** — Practice Problem 2 add/remove/borrow/return.
- **`WorkbookActivities`** — Tasks 1–4 plus the library practice problem.
- **`Lab7Demo`** — console demonstration of all activities.
- **`ComplexityMeasurement`** — real timing and equality-comparison experiments.
- **`test/LabTestRunner`** — dependency-free acceptance runner covering the six
  required cases, boundaries, invalid operations, snapshots, and the workbook
  mini-applications.

## Project structure

```
StudentDirectory/
├── AGENTS.md                  project rules for agent sessions
├── AGENT_LOG.md               plan, evidence, decisions, audit record
├── README.md                  this file
├── REFLECTION.md              evidence-based reflection (student completes)
├── SUBMISSION_CHECKLIST.md    final submission checks
├── .gitignore                 excludes out/, *.class, IDE noise
├── run-tests.bat              Windows compile + run
├── run-tests.sh               macOS/Linux compile + run
├── src/
│   ├── Student.java
│   ├── StudentDirectory.java
│   ├── CharacterFrequencyCounter.java
│   ├── ProductInventory.java
│   ├── LibraryBookTracker.java
│   ├── WorkbookActivities.java
│   ├── Lab7Demo.java
│   └── ComplexityMeasurement.java
└── test/
    └── LabTestRunner.java
```

The workbook names `starter-kit/labs/07-hashmap-key-value/`, but the actual
project folder supplied for this session was the empty `StudentDirectory/`
folder. No starter files or instructor tests existed, so the required paths
(`src/StudentDirectory.java`, `test/LabTestRunner.java`) were created from
scratch. No instructor file was deleted or changed.

## Requirements

- JDK **17 or newer**. Verified with `java 21.0.11` and `javac 21.0.11`.
- No external libraries or downloads. The test runner uses only the JDK.

## Compile and run

From the project root (`C:\Users\Administrator\StudentDirectory`):

### Windows

```bat
run-tests.bat
```

### Any platform

```bash
javac -d out src/*.java test/*.java
java -ea -cp out LabTestRunner
```

`-ea` is required. The runner contains a real assertion guard and exits with
code 2 if assertions are disabled.

### Demonstration

```bash
java -cp out Lab7Demo
```

### Measurements

```bash
java -cp out ComplexityMeasurement
```

## Actual test results

Exact command:

```text
> java -ea -cp out LabTestRunner
```

Final result from this machine:

```text
Test groups: 20, assertions: 124, passed: 124, failed: 0
RESULT: PASS
```

All 20 groups passed:

| Required case | Test group | What is asserted |
|---|---|---|
| Empty Map | `testEmptyDirectory` | size 0, null lookup, false removal, empty snapshot, unmodifiable snapshot |
| Duplicate Key | `testDuplicateKeyRejected` | second add returns false; size, name, grade, and object identity unchanged |
| Missing Key | `testMissingKey` | null lookup; update/remove false; no accidental insert |
| Record Update | `testRecordUpdate` | new name/grade visible; size unchanged; grade-only update works |
| Removal | `testRemoval` | record gone; size decremented; second removal false; other record intact |
| Repeated Characters | `testRepeatedCharacters` | BANANA → B=1, A=3, N=2, total 6 |

Additional coverage: detached/unmodifiable snapshots (including `clear` and
`keySet().remove`), sorted snapshots, exact-case IDs, empty/null frequency
input, case/space/punctuation counting, unmodifiable frequency maps, Student
validation, grade boundaries 0 and 100, invalid grades, null/blank directory
arguments, re-add after removal, immutability, equals/hashCode, formatting,
product inventory, and library tracker.

The **student checkpoint assertion** is in `testDuplicateKeyRejected`:
a duplicate add must keep the exact original `Student` object. The student
selected the duplicate-key behaviour; the Java syntax was drafted
collaboratively and must be reviewed by the student before submission.

## Counting rules (deliberately explicit)

`CharacterFrequencyCounter.count` processes every UTF-16 `char` exactly as it
appears:

- case-sensitive: `A` and `a` are different keys;
- spaces, punctuation, and digits are **counted**, not removed;
- no trimming, lower-casing, or letter-only filtering;
- a surrogate pair (many emoji) is two `char` values and is counted as two
  entries;
- `count("")` returns an empty map; `count(null)` throws
  `IllegalArgumentException`.

Example: `"Aa  a!"` (two spaces) → `' '=2, !=1, A=1, a=2`.

`BANANA` → `A=3, B=1, N=2` independent of iteration order.

## Complexity analysis

`n` = records or characters. `k` = distinct characters (≤ 65,536 for `char`).

| Operation | Average | Worst | Reason |
|---|---|---|---|
| `put` / `get` / `remove` | O(1) | O(n) | Hash to a bucket, then compare keys. A bad hash puts many keys in one bucket; a non-comparable chain can be scanned linearly. |
| `addStudent` (duplicate check + put) | O(1) | O(n) | Two lookups: `containsKey` + `put`. |
| `updateStudent` / `updateGrade` | O(1) | O(n) | Lookup + replacement. |
| `snapshot()` | O(n) | O(n²) only for an adversarial key class | Copy every entry into a new map. With String IDs the measured growth is linear. |
| `snapshotSortedById()` | O(n log n) | O(n log n · L) | Copy + sort; `L` is ID length for String comparisons. |
| Frequency count | O(n) | Θ(n) for the fixed `char` domain; O(n²) only if keys grow with n | Two map operations per character. `Character` keys cannot be made to collide by a growing key set. |
| Frequency `format` | O(k log k) | O(k log k) | Sorts distinct characters for stable display. |

### Auxiliary space (separate from time)

- Directory: O(n) for n stored records plus table capacity.
- `snapshot()`: O(n) extra for the copy.
- `snapshotSortedById()`: O(n) extra for the list plus TimSort's O(n)
  temporary array.
- Frequency counter: O(k), not O(n); repeated characters reuse one map entry.
- No recursion; call-stack space is O(1).

## Why is HashMap usually O(1)?

A HashMap computes `key.hashCode()`, spreads the bits, and maps the result to a
bucket index. With a good hash, each key lands in a nearly unique bucket, so a
lookup hashes once and compares one key — independent of n. This is **average**
O(1), not a guarantee.

A collision happens when two different keys reach the same bucket. Java stores
colliding entries in a chain (a linked list, and a tree bin once a bucket grows
past the treeify threshold). A lookup must then compare keys in that bucket
until it finds an `equals` match. If every key collides, the work grows with the
number of entries: O(n) worst case. Java 8+ tree bins improve this to O(log n)
when keys are `Comparable`; with equal hashes and non-comparable keys,
`TreeNode.find` may still scan both subtrees, which is why the measured
identical-hash rows remain linear.

## Measured evidence

Environment printed by the program: **Java 21.0.11, Windows 10 amd64, 4
processors, max heap 974 MiB**. Median of 3 trials after one fixed 10,000-element
warm-up. Counter increments and `Integer` boxing occur inside timed loops, so
ratios are more reliable than absolute nanoseconds. These are machine-specific
observations, not a proof.

### 1. Hash shape: good vs clustered vs identical

`equals/get` counts `equals` calls during successful lookups with fresh, equal
key objects. `hashCode/get` is 1.00 because HashMap hashes the query once.

| shape | n | put ns/op | get ns/op | remove ns/op | equals/get | hashCode/get |
|---|---:|---:|---:|---:|---:|---:|
| good | 1,000 | 516.9 | 275.3 | 362.4 | 1.00 | 1.00 |
| good | 4,000 | 205.9 | 68.6 | 98.4 | 1.00 | 1.00 |
| good | 16,000 | 131.5 | 88.1 | 135.9 | 1.00 | 1.00 |
| clustered (16 hash values) | 1,000 | 1,368.1 | 418.0 | 506.3 | 32.74 | 1.00 |
| clustered (16 hash values) | 4,000 | 3,072.0 | 2,798.9 | 1,202.7 | 126.50 | 1.00 |
| clustered (16 hash values) | 16,000 | 22,493.4 | 30,133.1 | 21,295.8 | 501.50 | 1.00 |
| identical | 1,000 | 4,355.5 | 4,029.5 | 2,151.5 | 501.50 | 1.00 |
| identical | 4,000 | 31,224.3 | 34,086.2 | 13,494.4 | 2,001.50 | 1.00 |
| identical | 16,000 | 335,895.9 | 269,004.3 | 106,659.6 | 8,001.50 | 1.00 |
| identical, Comparable control | 1,000 | 731.3 | 592.1 | 1,055.2 | 10.55 | 1.00 |
| identical, Comparable control | 4,000 | 698.0 | 346.9 | 617.5 | 12.83 | 1.00 |
| identical, Comparable control | 16,000 | 693.7 | 431.9 | 506.5 | 14.82 | 1.00 |

Interpretation:

- Good hashes: exactly 1 equality check per get at every size → O(1) key
  comparisons.
- 16 hash values: `equals/get` ≈ n/32 → bucket scanning is linear.
- One hash value, non-comparable: `equals/get` ≈ n/2 → full bucket scan.
  Treeification does **not** rescue this case.
- One hash value, comparable: `equals/get` ≈ log₂ n (10.55, 12.83, 14.82) →
  the tree bin can navigate. This is the control that isolates tree behaviour.

### 2. Snapshot cost

| n | snapshot ns | snapshot ns/entry | sorted snapshot ns/entry |
|---:|---:|---:|---:|
| 10,000 | 2,417,480 | 241.7 | 764.9 |
| 100,000 | 20,883,630 | 208.8 | 504.7 |
| 300,000 | 55,810,690 | 186.0 | 457.0 |

Snapshot ns/entry is roughly flat (186–242 ns), consistent with O(n). Sorted
snapshot per-entry time grows slowly, consistent with the extra log n factor.
Repeating 20 snapshots per trial reduced GC noise; one call per trial produced a
misleading non-monotonic row in an earlier run.

### 3. Character frequency

Two map operations per character (`getOrDefault` + `put`).

| n | ns/char | map operations (2n) | distinct characters |
|---:|---:|---:|---:|
| 10,000 | 224.6 | 20,000 | 26 |
| 100,000 | 184.2 | 200,000 | 26 |
| 1,000,000 | 18.6 | 2,000,000 | 26 |
| 1,048,576 | 94.3 | 2,097,152 | 65,536 |

The all-char row uses every possible `char` value 16 times. There is no
quadratic blow-up: `Character.hashCode()` is the char value and the key domain
is fixed, so the workbook's O(n²) pathological case is not reachable for this
key type. The falling ns/char is JIT warm-up, not a complexity change.

### 4. HashMap vs ArrayList linear search

100 searches for IDs near the end of the list, target strings precomputed
outside the timer:

| n | ArrayList ns/search | HashMap ns/search |
|---:|---:|---:|
| 10,000 | 31,073.0 | 102.0 |
| 100,000 | 558,547.0 | 62.0 |
| 300,000 | 1,421,768.0 | 107.0 |

Linear search grows with n; HashMap lookup stays roughly constant.

### Predicted vs measured differences

- Good-hash time did not increase with n, matching O(1); exact nanoseconds are
  noisy and not portable.
- Collision shapes matched O(n) comparisons; the identical non-comparable case
  was even slower per operation than a pure linear count because of cache misses.
- Snapshot and frequency measurements showed JIT/GC effects, so ratios and
  repeated trials were used instead of single timings.
- The workbook's O(n²) frequency worst case is true for an unbounded key domain,
  but not for `HashMap<Character, Integer>`.

## Challenge question: how are collisions handled?

Two different keys can have the same hash code, or different hash codes that map
to the same bucket after masking. HashMap stores colliding entries in the same
bucket. In the simple model, the bucket is a linked list: lookup hashes the key,
finds the bucket, then walks the chain comparing `equals` until it finds the
match. That is why a collision-heavy map degrades to O(n).

Java 8+ adds a second layer: when a bucket grows beyond the treeify threshold
(and the table is large enough), it becomes a red-black tree. If the keys are
`Comparable`, lookup can navigate the tree in O(log n) comparisons. If the keys
are not comparable and their hashes tie, the tree search may still examine both
subtrees, so the linear worst case can remain. Good hash distribution and
consistent `equals`/`hashCode` are the practical defences. The measured
`identical` vs `identical-Comparable` rows show exactly this difference.

## Documentation

- [`AGENT_LOG.md`](AGENT_LOG.md) — plan, pseudocode, initial failure, exact
  verification output, build decisions, audit decisions, agent limitation.
- [`REFLECTION.md`](REFLECTION.md) — the five required reflection answers with
  evidence and clearly marked places where the student must add personal input.
- [`SUBMISSION_CHECKLIST.md`](SUBMISSION_CHECKLIST.md) — final checks.

## Known limitations

- The directory is not thread-safe (`HashMap` is unsynchronized). Concurrent
  writes need external synchronization or `ConcurrentHashMap`.
- The `containsKey` + `put` duplicate check is not atomic.
- Grade validation is a deliberate design choice, not a workbook requirement.
- IDs are exact strings: no trimming or case normalization.
- Benchmarks use 3 trials and a single fixed order on one machine; they show
  trends, not portable constants.
- The student checkpoint assertion still needs the student's personal review.

## GitHub

No Git operation was performed. The student will review the project and publish
it manually. See the final report for suggested commands.
