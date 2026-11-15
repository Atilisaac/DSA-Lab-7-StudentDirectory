# AGENT_LOG.md — Lab 7: HashMap and Key–Value Structures

This log records the work in the order it happened. It is not a polished
after-the-fact story: the plan was written before implementation, the first
test run was captured from the terminal, and later sections record what was
actually observed.

**Student:** ____________________  **Section:** __________  **Date:** __________

---

## 0. Source material and starting state

- Workbook read: `Lab_07_HashMap_KeyValue_Workbook.docx` (Lab 7 technical
  activity; the reading pages are labelled "Lab 8" in the workbook).
- Project folder at start: **empty**. It contained no `src/`, `test/`,
  `AGENTS.md`, `opencode.json`, `.opencode/` agents, starter classes, supplied
  tests, or `run-tests` scripts.
- Because no starter kit was supplied, this project was created from scratch.
  The required paths were kept exactly as the workbook names them:
  `src/StudentDirectory.java` and `test/LabTestRunner.java`.
- Toolchain: `java 21.0.11` and `javac 21.0.11` (satisfies Java 17+).
- Agent environment: the `Plan`, `test-reviewer`, and `complexity-auditor`
  agents named by the workbook were **not configured** in this workspace. The
  built-in `general`/`explore` subagents were used for read-only reviews, and
  that substitution is recorded in §7. No review result is invented.

### Workbook requirements extracted

**Technical activity**

1. Keyed student directory backed by `HashMap<String, Student>`.
2. Unique student IDs; duplicate keys rejected (no silent overwrite).
3. Retrieve, update, and remove records by ID.
4. Return snapshots, never the mutable backing map.
5. Character-frequency map using `HashMap<Character, Integer>`.
6. Explain average versus worst-case behaviour and collisions/chaining.
7. Automated normal, boundary, and invalid-operation assertions.
8. Acceptance cases: Empty Map, Duplicate Key, Missing Key, Record Update,
   Removal, Repeated Characters.
9. Actual measurements compared with complexity predictions.
10. `AGENT_LOG.md`, `REFLECTION.md`, submission checklist, passing output with
    `-ea`.

**Workbook lab activities**

- Task 1: `HashMap<String, Double>` grades for Alice 89.5, Ben 91.0,
  Carla 85.5; demonstrate `put`, `get`, `containsKey`, `remove`.
- Task 2: iterate with a for-each over `entrySet()`.
- Task 3: `Student` class (ID, name, grade) and `HashMap<String, Student>`;
  add, update, retrieve, display.
- Task 4: product inventory `HashMap<String, Integer>` (code → quantity);
  add products, update stock, display.
- Practice Problem 3: frequency counter; `BANANA` → B=1, A=3, N=2.

---

## 1. Expected behaviour (written before implementation)

### Student

- Immutable value object: `id`, `name`, `grade`.
- `id` and `name` must be non-null and non-blank.
- `grade` must be a finite number in `[0, 100]`.
- Invalid construction fails fast with `IllegalArgumentException`.
- Updating a grade or name returns a **new** `Student`; it does not mutate the
  original.

### StudentDirectory

| Method | Behaviour |
|---|---|
| `boolean addStudent(Student s)` | Adds only when the ID is new. Returns `true` on success, `false` on duplicate. A duplicate never changes the stored record. `null` input throws `IllegalArgumentException`. |
| `Student getStudent(String id)` | Returns the stored immutable `Student`, or `null` when absent. |
| `boolean containsStudent(String id)` | `true` only when the ID exists. |
| `boolean updateStudent(Student s)` | Replaces an existing record with the same ID and returns `true`; returns `false` when the ID is absent (does not insert). |
| `boolean updateGrade(String id, double g)` | Replaces only the grade of an existing record. A missing ID returns `false`; an invalid grade throws `IllegalArgumentException`. The grade is validated **before** the existence check, so absent + invalid throws. |
| `boolean removeStudent(String id)` | Removes and returns `true` when present; `false` when absent. |
| `int size()` | Number of records. |
| `Map<String, Student> snapshot()` | An unmodifiable **copy** of the current entries. The returned map is detached: later directory writes are not visible in it, and the caller cannot modify the directory through it. |
| `List<Student> snapshotSortedById()` | Unmodifiable list from a fresh `ArrayList`, sorted by ID for deterministic display/tests. |

**Invalid-argument policy.** Passing `null`/blank IDs to lookup or mutation
methods, or passing a null `Student`, throws `IllegalArgumentException`. This is
a programmer error, not an absent-record case. Absent-but-valid IDs return
`null`/`false` as shown above. IDs are compared exactly: no trimming and no
case folding, so `"id001"` and `"ID001"` are different keys.

### CharacterFrequencyCounter

- `count(String text)` returns an unmodifiable `Map<Character, Integer>`.
- Counting rule (deliberate, documented, no hidden filtering): **every UTF-16
  `char` is counted exactly as it appears** — case-sensitive, and spaces,
  punctuation, and digits are included. `"Aa  a!"` (two spaces) gives `A=1,
  a=2, space=2, !=1`. A surrogate pair (emoji) counts as two `char` values. No
  trimming, no case folding, no letter-only filter. `format()` sorts the
  distinct characters only for readable output.
- `count(null)` throws `IllegalArgumentException`; `count("")` returns an empty
  map.
- `BANANA` must give `B=1, A=3, N=2` independent of iteration order.

### Workbook demonstrations

- Task 1–4 are implemented in `WorkbookActivities` (with a small
  `ProductInventory` class) and are runnable from `Lab7Demo`.
- Demonstration output is deterministic for readability (grades map and
  inventory keys sorted for display); HashMap lookup semantics are unchanged.

---

## 2. Implementation plan

1. Create `.gitignore`, `AGENTS.md`, `run-tests.bat`, and `run-tests.sh`.
2. Write `test/LabTestRunner.java` **before** production code. It is a
   dependency-free runner with explicit checks (no JUnit download needed), and
   it exits non-zero on failure.
3. Write a skeleton `src/` with the public API and explicit `TODO`
   `UnsupportedOperationException`s.
4. Compile and run once; record the exact first failure in §5.
5. Add the student checkpoint assertion (see §4) before implementing.
6. Implement `Student`, `StudentDirectory`, `CharacterFrequencyCounter`,
   `ProductInventory`, `WorkbookActivities`, and `Lab7Demo`.
7. Run the full suite, fix causes rather than tests.
8. Write and run `src/ComplexityMeasurement.java`; record real numbers.
9. Request read-only reviews, evaluate each recommendation, then write
   `README.md` and `REFLECTION.md`.
10. Final clean compile/run, remove `out/`, check `.gitignore`, and stop before
    any Git operation.

### Planned file layout

```
StudentDirectory/
├── AGENTS.md
├── AGENT_LOG.md
├── README.md
├── REFLECTION.md
├── SUBMISSION_CHECKLIST.md
├── .gitignore
├── run-tests.bat
├── run-tests.sh
├── src/
│   ├── Student.java
│   ├── StudentDirectory.java
│   ├── CharacterFrequencyCounter.java
│   ├── ProductInventory.java
│   ├── LibraryBookTracker.java        (added after plan review)
│   ├── WorkbookActivities.java
│   ├── Lab7Demo.java
│   └── ComplexityMeasurement.java
└── test/
    └── LabTestRunner.java
```

---

## 3. Pseudocode

### 3.1 StudentDirectory.addStudent

```
procedure addStudent(s):
    if s is null: throw IllegalArgumentException
    id <- s.getId()
    if backingMap.containsKey(id):
        return false                  # explicit duplicate rejection
    backingMap.put(id, s)
    return true
```

Note: `containsKey` followed by `put` is deliberately explicit for teaching
and for the duplicate invariant. It is not atomic for concurrent use; this
class is not thread-safe (documented limitation).

### 3.2 update / remove / snapshot

```
procedure updateStudent(s):
    if s is null: throw IllegalArgumentException
    if not backingMap.containsKey(s.getId()): return false
    backingMap.put(s.getId(), s)
    return true

procedure updateGrade(id, grade):
    if id is invalid: throw IllegalArgumentException
    if grade is invalid: throw IllegalArgumentException   # validated first
    existing <- backingMap.get(id)
    if existing is null: return false
    backingMap.put(id, existing.withGrade(grade))
    return true

procedure removeStudent(id):
    return backingMap.remove(id) is not null

procedure snapshot():
    copy <- new HashMap(backingMap)
    return unmodifiableView(copy)     # caller cannot touch backingMap
```

### 3.3 Character frequency

```
procedure count(text):
    if text is null: throw IllegalArgumentException
    frequencies <- new HashMap<Character, Integer>()
    for i from 0 to text.length - 1:
        c <- text.charAt(i)
        old <- frequencies.getOrDefault(c, 0)
        frequencies.put(c, old + 1)
    return unmodifiableView(frequencies)
```

### 3.4 Inventory

```
procedure addProduct(code, quantity):
    validate code, quantity >= 0
    if stock.containsKey(code): return false
    stock.put(code, quantity); return true

procedure updateStock(code, quantity):
    if not stock.containsKey(code) or quantity < 0: return false
    stock.put(code, quantity); return true
```

---

## 4. Test-first checkpoint

The acceptance runner is organised around the six required cases plus
boundary/invalid cases:

| Test | Required case | What it proves |
|---|---|---|
| `testEmptyDirectory` | Empty Map | size 0, missing lookup, failed removal, empty snapshot |
| `testDuplicateKeyRejected` | Duplicate Key | second add returns false and keeps the first grade |
| `testMissingKey` | Missing Key | `get` null, update/remove return false |
| `testRecordUpdate` | Record Update | replacement is visible; absent update does not insert |
| `testRemoval` | Removal | record disappears; second removal false |
| `testRepeatedCharacters` | Repeated Characters | BANANA → B=1, A=3, N=2 |
| `testSnapshotIsDetached` | boundary | snapshot is unmodifiable and detached from later writes |
| `testEmptyFrequency` | boundary | empty input → empty map; null rejected |
| `testCountingRules` | boundary | case and whitespace are counted, not silently filtered |
| `testStudentValidation` | invalid | blank ID/name and out-of-range grade rejected |
| `testSnapshotSortedAndIdRules` | boundary | sorted copy is unmodifiable; IDs are case-sensitive |
| `testGradeBoundaries` | boundary | 0/100 accepted; invalid updates throw; absent update false |
| `testDirectoryInvalidArguments` | invalid | null/blank IDs and null Student rejected |
| `testReAddAfterRemoval` | boundary | a removed ID can be added again |
| `testFrequencyMapUnmodifiable` | boundary | frequency result cannot be edited |
| `testInventory` / `testLibraryTracker` | workbook | Task 4 and Practice Problem 2 |
| `testFormatting` | boundary | deterministic `format()` output |
| `testStudentValueSemantics` | boundary | equals/hashCode/withName behave consistently |

**Student checkpoint assertion.** The workbook requires at least one
meaningful assertion authored/owned by the student before implementation help.
The student was asked which behaviour it should cover (§8 records the exact
exchange) and chose the duplicate-key invariant. The assertion lives in
`testDuplicateKeyRejected` and adds the identity check
`original == directory.getStudent("2025-001")` on top of the mandatory
duplicate case. The student did not type the Java syntax independently, so this
is recorded as **collaborative**, not independently authored. The student must
review and be able to explain it before submission.

---

## 5. Initial failure evidence (captured before implementation)

No starter kit existed, so a compile-only skeleton was created with the public
API and `TODO` exceptions. The tests were written first and run against that
skeleton. Exact command and output (PowerShell, project root):

```text
> javac -d out src/*.java test/*.java
> java -ea -cp out LabTestRunner
Lab 7 acceptance runner
=======================
[FAIL] Empty Map threw UnsupportedOperationException: TODO: implement size
[FAIL] Duplicate Key threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Missing Key threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Record Update threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Removal threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Repeated Characters threw UnsupportedOperationException: TODO: implement count
[FAIL] Snapshot isolation threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Empty frequency input threw UnsupportedOperationException: TODO: implement count
[FAIL] Counting rules threw UnsupportedOperationException: TODO: implement count
[FAIL] blank id rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] null id rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] blank name rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] negative grade rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] grade above 100 rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] NaN grade rejected expected IllegalArgumentException but got UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Student immutability threw UnsupportedOperationException: TODO: implement Student constructor
[FAIL] Product inventory threw UnsupportedOperationException: TODO: implement addProduct

Tests run: 17, passed: 0, failed: 17
RESULT: FAIL
```

Exit code was `1`, as intended. After this run, tests were **added** for
snapshots, boundaries, invalid arguments, formatting, value semantics, and the
library tracker. No failing assertion was weakened. One fixture was corrected:
`testCountingRules` originally used `"Aa a!"` (one space) while asserting two
spaces. The input was changed to `"Aa  a!"` (two spaces) so the test now matches
its stated intent; the implementation already counted every character exactly.
That correction and the second failing run are recorded in §9.

---

## 6. Complexity predictions

`n` = number of records / characters; `k` = number of distinct characters
(≤ 65,536 for `char`); `b` = bucket count.

| Operation | Average | Worst | Why |
|---|---|---|---|
| `put` / `get` / `remove` | O(1) | O(n) | Hash to a bucket, then compare keys in that bucket. Bad hashing puts many keys in one bucket; scanning a chain is linear. Java 8+ treeifies a large bucket. For **comparable** keys the tree can navigate in O(log n); for non-comparable keys with equal hashes, `TreeNode.find` may still scan both subtrees, so the workbook's O(n) worst case remains observable. |
| Duplicate check + add | O(1) avg | O(n) | Two lookups: one `containsKey` plus one `put`. |
| `updateStudent` / `updateGrade` | O(1) avg | O(n) | One lookup plus one replacement. |
| `removeStudent` | O(1) avg | O(n) | One removal. |
| `snapshot()` | O(n) avg for String IDs | O(n²) adversarial key class | Copies every entry into a new map; re-insertion degrades if every copied key collides. |
| `snapshotSortedById()` | O(n log n) | O(n log n · L) with ID length L | Copies, then sorts n records with String comparisons. |
| Frequency count | O(n) avg | **Θ(n) for this `Character` key domain**; O(n²) only if keys grow with n | Two map operations per character (`getOrDefault` + `put`). The 65,536-value `char` domain is fixed, so the workbook's O(n²) pathological case cannot be constructed with `Character` keys. |
| Frequency `format` | O(k log k) | O(k log k) | Sorts distinct characters for stable output. |

### Auxiliary space (reasoned, not measured)

- `StudentDirectory`: O(n) for n stored records plus the table's capacity
  overhead.
- `snapshot()`: O(n) additional space for the copy.
- `snapshotSortedById()`: O(n) additional space for the fresh `ArrayList` plus
  TimSort's O(n) temporary array.
- Frequency counter: O(k) additional space for distinct characters, not O(n),
  because repeated characters reuse map entries. Here k ≤ 65,536 and is 26 for
  the 26-letter test text.
- No recursion; call-stack space is O(1).

### Measurement contract (defined before measuring)

The program reports three separate quantities and does not blur them:

1. **Elapsed wall-clock time** from `System.nanoTime`, **median of 3 trials**
   after one fixed 10,000-element warm-up per shape. It includes JIT, GC, and OS
   noise; it is an observation, not a proof. Snapshot timing repeats 20
   snapshots per trial to average out GC pauses.
2. **Key equality comparisons** — a deterministic counter incremented inside a
   custom key's `equals`. Lookups use **fresh but equal** key objects, so
   HashMap cannot short-circuit on object identity. This is the primary
   evidence for bucket scanning.
3. **Key hash-code calls** — incremented inside `hashCode`, expected to be 1 per
   lookup because HashMap hashes the query once.
4. **Map operations performed** — the loop bound: n puts/gets/removes, and 2n
   map operations for the frequency counter. This measures workload, not
   internal cost.

Shapes and sizes: good hashes, 16 distinct hash values, and one constant hash;
each at n = 1,000 / 4,000 / 16,000. A Comparable constant-hash control isolates
tree navigation. Frequency sizes are 10,000 / 100,000 / 1,000,000 plus a
1,048,576-character text containing all 65,536 char values. Linear search is
compared at 10,000 / 100,000 / 300,000. `System.gc()` is never forced.
Counter increments and `Integer` boxing occur inside timed loops; ratios are
more trustworthy than absolute nanoseconds, and this is printed by the program.

---

## 7. Build decisions

1. **Duplicate rejection returns `boolean`, not a silent overwrite.** `addStudent`
   performs an explicit `containsKey` check and returns `false` on a duplicate.
   The caller cannot ignore an exception as easily as a boolean, but the lab is
   a console exercise and the test asserts the return value. The important part
   is that the `put` is never reached for a duplicate, so the old value survives.
2. **`updateStudent` never inserts.** It uses the same private `replaceExisting`
   step as `updateGrade`, which checks `containsKey` first. A missing ID returns
   `false`; this keeps "update" distinct from "add".
3. **Fail fast on programmer errors.** Null/blank IDs, null Students, and invalid
   grades throw `IllegalArgumentException`; valid-but-missing IDs return
   `null`/`false`. `updateGrade` validates the grade before checking existence,
   so absent + invalid throws. This policy is stated in every javadoc and tested.
4. **Snapshot strategy: copy, then wrap.** `snapshot()` copies into a fresh
   `HashMap` and wraps it with `Collections.unmodifiableMap`. The copy detaches
   the result from later directory writes; the wrapper blocks edits through the
   returned reference. `snapshotSortedById()` copies values into a fresh
   `ArrayList`, sorts it, and wraps it unmodifiable. `Student` is immutable, so
   sharing values is safe.
5. **Immutability instead of defensive copying of values.** A `Student` cannot
   be changed after construction; updates return new objects. This removes a
   whole class of snapshot bugs.
6. **Counting rule is explicit and literal.** Every UTF-16 `char` is counted,
   case-sensitively, including spaces and punctuation. The workbook says "each
   letter", but the master prompt warns against silently filtering. Counting
   everything is the least surprising choice and is tested with `"Aa  a!"`.
7. **Display sorting is separate from storage.** HashMaps remain the backing
   structure; display helpers sort a copy of entries/keys only so console output
   is stable. Task 2 still iterates `entrySet()`.
8. **No JUnit dependency.** The runner uses explicit checks and exits non-zero on
   failure. `-ea` is enforced by a real `assert` guard, so the required command
   cannot silently run with assertions disabled.
9. **Library tracker added.** Practice Problem 2 was initially considered
   optional, but it reuses the same key→count pattern and was cheap to add; it
   is implemented and tested rather than left as a gap.

---

## 8. Plan review and student checkpoint

**Student checkpoint (recorded before Build).** The assistant explained that the
workbook requires a meaningful, student-owned assertion and asked the student
which behaviour it should cover. The student chose:

> "Duplicate add keeps original" — adding the same ID twice returns `false`,
> the size stays 1, and the original name/grade remain stored.

The assistant then wrote the Java syntax in
`test/LabTestRunner.java#testDuplicateKeyRejected`. This is therefore
**collaborative** work: the student selected and owns the requirement, but did
not type the assertion independently. The student must review it and be ready
to explain it (or replace it with their own wording) before submission. It is
not described as independently authored.

**Plan review.** The `Plan` agent named by the workbook is not configured in
this workspace, and no `opencode.json` or `.opencode/agents` directory existed.
A read-only `general` subagent was used instead. Its findings and the student's
accept/reject decisions are in §10. The review was requested before the
implementation files were written.

---

## 9. Verification log

### 9.1 First run after implementation

```text
> javac -d out src/*.java test/*.java
> java -ea -cp out LabTestRunner
...
[PASS] Repeated Characters
[PASS] Snapshot isolation
[PASS] Sorted snapshot and ID rules
[PASS] Empty frequency input
[FAIL] spaces are counted expected <2> but was <1>
[FAIL] total characters expected <6> but was <5>
[PASS] Frequency map is unmodifiable
...
Tests run: 94, passed: 92, failed: 2
RESULT: FAIL
```

Cause: the `testCountingRules` fixture was `"Aa a!"` (one space) while the
assertion expected two spaces. The implementation was correct. Fixture changed
to `"Aa  a!"` (two spaces); assertion left at 2 spaces and total 6. The demo was
updated to match. This is a test-data correction, not a weakened expectation.

### 9.2 Final test run

Command:

```text
> java -ea -cp out LabTestRunner
```

Output:

```text
Lab 7 acceptance runner
=======================
[PASS] Empty Map
[PASS] Duplicate Key
[PASS] Missing Key
[PASS] Record Update
[PASS] Removal
[PASS] Repeated Characters
[PASS] Snapshot isolation
[PASS] Sorted snapshot and ID rules
[PASS] Empty frequency input
[PASS] Counting rules
[PASS] Frequency map is unmodifiable
[PASS] Student validation
[PASS] Grade boundaries
[PASS] Directory invalid arguments
[PASS] Re-add after removal
[PASS] Student immutability
[PASS] Product inventory
[PASS] Library tracker
[PASS] Formatting helpers
[PASS] Student value semantics

Test groups: 20, assertions: 124, passed: 124, failed: 0
RESULT: PASS
```

### 9.3 Assertions-enabled guard

Verified manually: running `java -cp out LabTestRunner` without `-ea` prints
`ERROR: assertions are disabled...` and exits with code 2. The required command
uses `-ea`.

### 9.4 Demonstration output

Command: `java -cp out Lab7Demo`. Excerpt (the `...` marks omitted lines;
the full output is reproducible with the command above):

```text
=== Task 1 - HashMap basics ===
Initial grades: {Alice=89.5, Ben=91.0, Carla=85.5}
Ben's grade (get): 91.0
Contains Alice? true
Contains Diana? false
Removed Carla's grade (remove): 85.5
After removal: {Alice=89.5, Ben=91.0}

=== Task 2 - Iterating with entrySet() ===
Alice: 89.5
Ben: 91.0
Carla: 85.5

=== Task 3 - HashMap of Student objects ===
Added 5 students. Size = 5
Retrieved 2025-002: Student{id='2025-002', name='Ben', grade=91.0}
Updated Ben's grade? true -> 93.25
Duplicate 2025-001 accepted? false (original kept: Alice)
...
=== Task 4 - Product inventory ===
...
Update P-200 to 25? true
Update missing P-999? false
...
=== Character frequency counter ===
Input: BANANA
Counts: A=3, B=1, N=2
Input: Aa  a! (two spaces)
Counts: ' '=2, !=1, A=1, a=2
```

The actual run also includes the Practice Problem 2 library tracker section
(`Clean Code` / `Effective Java`).

### 9.5 Measurement run

Command: `java -cp out ComplexityMeasurement`.

Environment: Java 21.0.11, Windows 10 amd64, 4 processors, max heap 974 MiB.
Median of 3 trials after one fixed 10,000-element warm-up per shape. Counter
increments and `Integer` boxing occur inside timed loops; ratios are more
reliable than absolute nanoseconds.

Table 1:

```text
| shape | n | put ns/op | get ns/op | remove ns/op | equals/get | hashCode/get |
| good | 1000 | 516.9 | 275.3 | 362.4 | 1.00 | 1.00 |
| good | 4000 | 205.9 | 68.6 | 98.4 | 1.00 | 1.00 |
| good | 16000 | 131.5 | 88.1 | 135.9 | 1.00 | 1.00 |
| clustered | 1000 | 1368.1 | 418.0 | 506.3 | 32.74 | 1.00 |
| clustered | 4000 | 3072.0 | 2798.9 | 1202.7 | 126.50 | 1.00 |
| clustered | 16000 | 22493.4 | 30133.1 | 21295.8 | 501.50 | 1.00 |
| identical | 1000 | 4355.5 | 4029.5 | 2151.5 | 501.50 | 1.00 |
| identical | 4000 | 31224.3 | 34086.2 | 13494.4 | 2001.50 | 1.00 |
| identical | 16000 | 335895.9 | 269004.3 | 106659.6 | 8001.50 | 1.00 |
| identical-Comparable | 1000 | 731.3 | 592.1 | 1055.2 | 10.55 | 1.00 |
| identical-Comparable | 4000 | 698.0 | 346.9 | 617.5 | 12.83 | 1.00 |
| identical-Comparable | 16000 | 693.7 | 431.9 | 506.5 | 14.82 | 1.00 |
```

Table 2:

```text
| n | snapshot ns | snapshot ns/entry | sorted snapshot ns/entry |
| 10000 | 2417480 | 241.7 | 764.9 |
| 100000 | 20883630 | 208.8 | 504.7 |
| 300000 | 55810690 | 186.0 | 457.0 |
```

Table 3:

```text
| n | ns/char | map operations (2n) | distinct characters |
| 10000 | 224.6 | 20000 | 26 |
| 100000 | 184.2 | 200000 | 26 |
| 1000000 | 18.6 | 2000000 | 26 |
| 1048576 | 94.3 | 2097152 | 65536 |
```

Table 4:

```text
| n | ArrayList ns/search | HashMap ns/search |
| 10000 | 31073.0 | 102.0 |
| 100000 | 558547.0 | 62.0 |
| 300000 | 1421768.0 | 107.0 |
```

**Interpretation.** Good hashes need exactly one `equals` per successful get.
Sixteen hash values produce about n/32 comparisons; one hash value produces
about n/2. The comparable control falls to about log₂ n. Snapshot per-entry cost
is roughly flat. The all-char frequency run does not show quadratic growth. The
ArrayList baseline grows with n while HashMap lookup does not. Absolute
nanoseconds are noisy and machine-specific.

---

## 10. Audit recommendations

The workbook names `@test-reviewer` and `@complexity-auditor`. Neither agent was
configured in this workspace (there was no `opencode.json` or `.opencode/agents`
directory). Read-only `general` subagents were used instead, with explicit
instructions not to edit files. This substitution is a limitation of the
environment, not a claim that the named agents ran.

### 10.1 Plan review (read-only, before implementation)

The review started before `src/` existed, so a few of its opening statements
were stale (it said the directories did not exist). The substantive findings
were evaluated against the finished code.

| # | Finding | Decision | Reason |
|---|---|---|---|
| P1–P2 | §8 claimed an exchange that had not yet been recorded; §5 was a placeholder | Accepted | Both were filled only after the events actually occurred; the log now contains the real exchange and output. |
| P3 | `updateGrade` contract was self-contradictory | Accepted | Fixed: missing → false; invalid → throw; validation before existence. Docs, pseudocode, javadoc, and tests now agree. |
| P4 | Task 2 could lose `entrySet()` by sorting | Accepted | The implementation iterates a sorted copy of `entrySet()`; storage is still `HashMap`. |
| P5 | Practice Problem 2 was missing | Accepted | Implemented `LibraryBookTracker` and tested it. |
| P6 | Practice Problem 1's five students were not demonstrated | Accepted | Task 3 adds five records, retrieves one, updates one, and displays all. |
| P7 | Challenge question had no home | Accepted | Answered in README; reflection points to it. |
| P8 | Frequency O(n²) is wrong for `Character` keys | Accepted with qualification | Kept the workbook's general claim for unbounded key domains; measured the bounded case separately. |
| P9 | Snapshot O(n) ignores adversarial re-insertion | Accepted | Table now states O(n) average for String IDs and O(n²) only for an adversarial key class. |
| P10 | `format` was undefined in the spec | Accepted | Added its contract and tests. |
| P11 | Treeification may contradict the measurement | Accepted | Added a `Comparable` control row; equal-hash non-comparable keys still scan. |
| P12 | Equality counter is incomplete | Accepted | Added `hashCode` counting and a side-effect definition; both are reported. |
| P13 | Reproducibility parameters were missing | Accepted | Fixed warm-up, 3 trials, median, fixed sizes, 20 snapshot repeats, environment output. |
| P14 | No ArrayList/LinkedList baseline | Accepted | Added Table 4; it shows the expected asymptotic gap. |
| P15 | `updateStudent`/`updateGrade` duplicated replacement logic | Accepted | Extracted private `replaceExisting`. |
| P16 | `-ea` was cosmetic | Accepted | Added a real assertion guard that exits 2 when disabled. |
| P17 | Boundary tests were missing | Accepted | Added null/blank IDs, grade 0/100, invalid grades, re-add, sorted snapshots. |
| P18 | Duplicate test checked only grade | Accepted | Added name, size, and exact-object identity. |
| P19 | Snapshot tests did not assert throws | Accepted | Added `put`, `remove`, `clear`, `keySet().remove`, and frequency-map attempts. |
| P20–P22 | Wording, grade-validation rationale, ID normalization | Accepted | Clarified in §1/§7 and README; exact-case IDs tested. |
| P23 | Demo layering risk | Accepted | `Lab7Demo` is a thin `main`; helpers live in `WorkbookActivities`. |
| P24 | Test-first ordering was ambiguous | Accepted | Skeleton and tests landed together; the recorded first failure is the TODO exception, not `javac`. |
| P25 | §5 placeholder could read as observed output | Accepted | Replaced with the real console text. |
| P26 | Workbook path deviation | Accepted | Recorded in §0 and README: no starter kit existed; required filenames preserved. |

### 10.2 Test reviewer (read-only)

| # | Finding | Decision | Reason |
|---|---|---|---|
| T1 | Required deliverables were missing at review time | Accepted | The review ran before README/REFLECTION/checklist were written; they now exist. |
| T2 | AGENT_LOG §9–§12 were placeholders | Accepted | Filled with actual output and decisions. |
| T3 | `updateGrade` contract contradicted the code | Accepted | Same fix as P3; test added for absent + invalid. |
| T4 | Null/blank ID rules were undocumented | Accepted | Added the invalid-argument policy to §1. |
| T5 | `updateGrade` precedence was untested | Accepted | Added `updateGrade missing with invalid grade throws first`. |
| T6 | Checkpoint test was misnamed and not visibly extra | Partially accepted | Corrected the log reference and added the object-identity assertion. Did not split it into a duplicate test method, because the student chose the duplicate behaviour and splitting would repeat the same mandatory case. |
| T7 | §5 "no test changed" was imprecise | Accepted | §5 now lists added tests and the fixture correction. |
| T8 | Formatting helpers were untested | Accepted | Added `testFormatting` and display-order assertions. |
| T9 | §7 Build decisions was empty | Accepted | Filled with nine decisions and reasons. |
| T10 | `equals`/`hashCode`/`withName` untested | Accepted | Added `testStudentValueSemantics`. |
| T11 | Snapshot detachment coverage was narrow | Accepted | Added `clear` and `keySet().remove` assertions. |
| T12 | Negative input branches were untested | Accepted | Added negative stock/copies and missing `-1` sentinel tests. |
| T13 | "Tests run" was really assertions | Accepted | Summary now says "Test groups: 20, assertions: ...". |

### 10.3 Complexity auditor (read-only)

| # | Finding | Decision | Reason |
|---|---|---|---|
| C1 | Measured results were absent from the log | Accepted | Tables 1–4, environment, command, and interpretation are in §9.5. |
| C2 | Frequency O(n²) is unreachable for `Character` | Accepted with qualification | Verified with the all-65536-char run; kept the workbook claim only for unbounded domains. |
| C3 | Treeification statement needed the non-Comparable caveat | Accepted | Added the comparable control row; measured log₂ growth there and linear growth for non-comparable keys. |
| C4 | Warm-up scaled with n | Partially accepted | Changed to a fixed 10,000-element warm-up. Kept 3 trials (not 5) to bound runtime. The falling good-hash time persisted, so the auditor's causal explanation was incomplete; see §11. |
| C5 | Table 4 built strings inside the timer | Accepted | Precomputed 100 target strings before timing; reran. |
| C6 | Snapshot timing inherited GC debt | Accepted | Built the directory before the timer and timed 20 snapshots per trial; per-entry cost became roughly flat. |
| C7 | Instrumentation and boxing were undisclosed | Accepted | Printed in the program header and stated in the contract; put value changed to a cached `Integer`. |
| C8 | Contract said "averaged" but code used median; ops were a loop bound | Accepted | Contract now says median, fixed warm-up, and 2n frequency operations. |
| C9 | `updateGrade` prose contradicted code | Accepted | Same fix as P3/T3; precedence test added. |
| C10 | "One map update per char" hid two lookups | Accepted | Table 3 and complexity table now say 2n map operations. |
| C11 | `hashCode/get = 1` was unexplained | Accepted | Added the explanation: HashMap hashes the query once. |
| C12 | Space analysis wording and "reasoned, not measured" | Accepted | Separated TimSort's temporary array and labelled the section analytic. |
| C13 | No environment metadata | Accepted | Program prints Java version, OS, arch, processors, and max heap. |
| C14 | Duplicate check/add and remove were not measured | Partially accepted | Documented `containsKey` + `put` as two lookups. Did not add a separate equals/remove column; get is the binding case and remove timing is already reported. |
| C15 | Fixed order/thermal drift; shape labels overstate | Rejected | Fixed order keeps runs reproducible, and thermal drift is not the variable under study. Shape labels were clarified as "16 distinct hash values"; the limitation is stated in README. |

---

## 11. Independent evaluation of an agent limitation

**Agent claim.** The complexity auditor said the good-hash per-operation time
fell as n grew because the warm-up size was `n/10`, so larger inputs received a
larger warm-up.

**What I did.** I changed the warm-up to a fixed 10,000 elements, kept the same
sizes and trials, and reran `ComplexityMeasurement`. The good-hash put times
were **516.9 → 205.9 → 131.5 ns/op** and get times **275.3 → 68.6 → 88.1 ns/op**
for n = 1,000 → 4,000 → 16,000. The downward trend did not disappear.

**Conclusion.** The recommendation was a real methodology improvement (the
warm-up is now controlled), but the auditor's stated cause was incomplete. JIT
compilation, cache warming, and allocation behaviour also contribute. I accepted
the change to the method and rejected the implied claim that a fixed warm-up
would flatten the numbers. This is the independently evaluated agent limitation
required by the workbook.

A second limitation of the environment: the named `Plan`, `@test-reviewer`, and
`@complexity-auditor` agents were not configured. The `general` subagent reviews
were useful but are not identical to the workbook's named agents.

---

## 12. Final status

- Implementation: complete; no TODO exceptions.
- Tests: **20 groups / 124 assertions, 124 passed, 0 failed, RESULT: PASS** with
  `-ea`.
- Measurements: actual, recorded in §9.5 and README.
- Documentation: README, AGENT_LOG, REFLECTION, submission checklist present.
- Git: no repository was initialized, staged, committed, or pushed.
- Remaining student work: personal reflection wording, name/section/date,
  checkpoint-assertion review, and any instructor-specific workbook screenshots.
- Submission readiness: technically ready after the student completes the
  personal items in `SUBMISSION_CHECKLIST.md`.
