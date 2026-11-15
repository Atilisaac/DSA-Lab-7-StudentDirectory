# REFLECTION.md — Lab 7

> **How to use this file.** The answers below are an evidence-based draft, not a
> claim about the student's personal experience. Replace the bracketed
> **Student input** parts with your own words before submitting. The technical
> facts and citations are real and can be checked against `AGENT_LOG.md`,
> `README.md`, and the terminal output.

## 1. Which invariant or edge case caused the most difficult failure? Cite the failing test.

**Evidence.** The first run after implementation produced exactly one failing
test group, `Counting rules`:

```text
[FAIL] spaces are counted expected <2> but was <1>
[FAIL] total characters expected <6> but was <5>
```

The invariant was "count every character exactly, including whitespace". The
implementation was correct; the test fixture used `"Aa a!"` (one space) while
asserting two spaces. The fix was to change the fixture to `"Aa  a!"` (two
spaces), not to weaken the assertion. The corrected suite then passed.

A second, harder issue did not show up as a red test until an audit: the
`updateGrade` contract was inconsistent. The plan said an invalid grade returned
`false`, while `Student` threw `IllegalArgumentException`. The final rule is:
missing ID → `false`; invalid grade → throw; grade validated before existence.
Tests now cover both `existing + invalid` and `missing + invalid`.

**Draft answer.** The most difficult failure was the counting-rule test, because
the test data and the assertion disagreed and I had to decide which one was
wrong before touching code. The more important design failure was the
`updateGrade` contract, which the audit exposed.

**Student input:** Which of these felt most difficult to you, and why? Add the
specific moment you noticed the cause.

## 2. How did measured operations compare with your complexity prediction?

**Evidence from `ComplexityMeasurement`.**

- Good hashes: `equals/get` was **1.00** at n = 1,000, 4,000, and 16,000. That
  matches O(1) key comparisons per lookup.
- 16 distinct hash values: `equals/get` grew **32.74 → 126.50 → 501.50**, about
  n/32. That matches linear bucket scanning.
- One constant hash, non-comparable keys: `equals/get` grew **501.50 → 2,001.50
  → 8,001.50**, about n/2. Per-operation time also grew sharply. This matches
  the workbook's O(n) worst case.
- One constant hash, `Comparable` keys: `equals/get` was **10.55 → 12.83 →
  14.82**, about log₂ n. The tree bin helped only when it could navigate.
- Snapshot: per-entry time stayed roughly flat (241.7 → 208.8 → 186.0 ns),
  consistent with O(n). Sorted snapshot per-entry time grew slowly, consistent
  with the extra log n factor.
- Frequency counter: ns/char did **not** grow with n, including a 1,048,576-char
  text containing all 65,536 char values. The workbook's O(n²) worst case is not
  reachable with a fixed `Character` key domain.
- ArrayList linear search grew from about 31 µs to 1.42 ms per search, while
  HashMap stayed around 0.1 µs. The expected asymptotic difference is visible.

**Caveat.** Absolute nanoseconds are machine-specific. JIT warm-up, GC, cache
behaviour, 3 trials, and fixed measurement order all affect the numbers. The
comparison counts (`equals/get`) are more trustworthy than the timings because
they are exact integer counts.

**Draft answer.** The comparison measurements matched the prediction almost
exactly: one equality check for good hashes, linear growth under collisions, and
logarithmic growth for the Comparable control. The timing numbers were noisier
and sometimes fell as n grew because of JIT warm-up, so I relied on the operation
counts for the complexity conclusion.

**Student input:** Which measured row surprised you most? Explain what you
expected and what you saw.

## 3. Which agent recommendation did you verify independently, and what evidence determined your decision?

**Recommendation chosen.** The complexity auditor claimed that the workbook's
"frequency counter is O(n²) pathological" is not reachable for
`HashMap<Character, Integer>`, because `Character` has a fixed 65,536-value
domain and `Character.hashCode()` is the char value.

**How it was verified.** I did not accept the claim from the review text. I added
a measurement row that counts a 1,048,576-character string containing every
`char` value 16 times. The measured cost was **94.3 ns/char**, with 65,536
distinct keys and no upward trend with n. The three ordinary frequency rows were
224.6, 184.2, and 18.6 ns/char. This evidence supports the bounded-domain
argument.

**Decision.** Accepted, with a qualification: the workbook's O(n²) claim is kept
as a general statement for an **unbounded** key domain (keys whose number grows
with n), while this implementation is described as Θ(n) worst case for its fixed
`Character` domain.

**Independently evaluated agent limitation.** The same auditor attributed the
falling good-hash per-operation time to a warm-up that scaled with n. I changed
the warm-up to a fixed 10,000 elements and reran. The falling trend remained
(put: 516.9 → 205.9 → 131.5 ns/op), so the recommendation improved the method
but the stated cause was incomplete; JIT and cache effects also contribute. The
audit finding was accepted as a methodology improvement, not as a complete
explanation.

**Student input:** If you verified a different recommendation, name it and cite
the command or table you used.

## 4. What implementation choice would you change for a much larger dataset?

**Evidence.** The current directory stores everything in one `HashMap` and
`snapshot()` copies every entry. At 300,000 records the snapshot took about
55.8 ms and allocated a second map. `snapshotSortedById()` also allocates a list
and sorts it. The class is not thread-safe.

**Draft answer.** For a much larger dataset I would not copy the whole map for
every read. I would page or stream results, or keep an immutable index and
return a read-only view with a documented lifetime. I would pre-size the map to
avoid repeated resizing, use a database or an on-disk index when the data no
longer fits comfortably in memory, and use `ConcurrentHashMap` or explicit
locking if multiple threads write. I would also measure with more records and
more trials.

**Student input:** Choose the change you would actually make first, and say why.

## 5. Identify one limitation that remains in your submitted solution.

**Evidence.**

- `StudentDirectory` is not thread-safe. `containsKey` + `put` is not atomic, so
  two concurrent adds of the same ID could both pass the check.
- `snapshot()` is O(n) and allocates a full copy; this is deliberate safety, but
  it is expensive for large directories.
- The frequency counter counts every `char`, including spaces and punctuation.
  That is documented and tested, but it may not be what someone expecting
  "letters only" wants.
- The benchmarks use 3 trials, one machine, and a fixed order. They show trends,
  not portable constants.
- The student checkpoint assertion was drafted collaboratively and still needs
  the student's review.

**Draft answer.** The limitation I would name first is thread safety: the
duplicate check and the insert are separate operations, so the class is only
safe for the single-threaded console use it was designed for.


