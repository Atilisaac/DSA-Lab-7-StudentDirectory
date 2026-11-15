# Submission Checklist — Lab 7

Checked against the workbook's required submission and the project's actual
state. Do not tick a box unless the evidence is present.

## Required work

- [x] `src/` contains the completed implementation:
  `Student`, `StudentDirectory`, `CharacterFrequencyCounter`, `ProductInventory`,
  `LibraryBookTracker`, `WorkbookActivities`, `Lab7Demo`,
  `ComplexityMeasurement`.
- [x] `test/LabTestRunner.java` contains the six required acceptance cases plus
  boundary and invalid-operation tests.
- [x] All tests compile and pass with assertions enabled:
  `Test groups: 20, assertions: 124, passed: 124, failed: 0`, `RESULT: PASS`.
- [x] The runner refuses to run without `-ea` (exit code 2).
- [x] No `TODO` exception remains in `src/`.
- [x] Duplicate IDs are rejected without overwriting the stored record.
- [x] Missing IDs return `null`/`false` and do not insert.
- [x] Updates change only existing records; `updateGrade` validates the grade.
- [x] Snapshots are detached copies wrapped unmodifiable; the backing map is
  private.
- [x] Character-frequency counting rules are explicit and tested
  (case-sensitive; whitespace and punctuation counted).
- [x] Tasks 1–4 are implemented and demonstrated.
- [x] Practice Problem 2 (library tracker) is implemented and tested.
- [x] `AGENT_LOG.md` records the plan before Build, the real initial failure,
  build decisions, exact verification output, audit decisions, and one
  independently evaluated agent limitation.
- [x] Complexity predictions are compared with actual measurements at three or
  more sizes/shapes.
- [x] `README.md` contains the commands, actual results, coverage, complexity,
  collision explanation, and limitations.
- [x] `REFLECTION.md` answers the five questions with evidence and marks the
  parts that need the student's own input.
- [x] `.gitignore` excludes `out/`, `*.class`, IDE files, and local logs.
- [x] No credentials, API keys, or public session links are in the project.

## Student-only work (must be completed personally)

- [ ] Fill in name, section, and date in `AGENT_LOG.md` / the workbook cover.
- [ ] Review the student checkpoint assertion in `testDuplicateKeyRejected`
  (duplicate add keeps the exact original object) and be ready to explain it.
- [ ] Replace the **Student input** prompts in `REFLECTION.md` with your own
  words. Do not submit the draft as a personal reflection.
- [ ] Run the commands once on your own machine and confirm the output.
- [ ] Add screenshots/logs to the workbook's "Output" section if your instructor
  requires the workbook format.
- [ ] Confirm whether your instructor wants the workbook's "Discussion of
  Concepts" and "Challenge Question" copied into the workbook as well. The
  README contains the challenge answer.
- [ ] Publish to GitHub manually if required. No Git command was run here.

## Final cleanup state

- [x] `out/` is generated only by the build and is ignored by `.gitignore`.
- [x] Compiled `.class` files are not part of the source tree.
- [x] Instructor files: none were supplied, so none were deleted or changed.
- [x] Documentation matches the current implementation.
- [x] The local `out/` folder was removed after the final passing run
  (`out exists after cleanup: False`). Rebuild it with the commands above.

## Evidence commands

```text
javac -d out src/*.java test/*.java
java -ea -cp out LabTestRunner
java -cp out Lab7Demo
java -cp out ComplexityMeasurement
```
