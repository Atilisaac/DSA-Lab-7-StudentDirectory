# Project Rules — StudentDirectory (Lab 7)

These rules apply to every agent session in this folder.

## Scope

- Lab 7: HashMap and Key–Value Structures.
- Required source entry point: `src/StudentDirectory.java`.
- Required acceptance runner: `test/LabTestRunner.java`.
- Workbook: `Lab_07_HashMap_KeyValue_Workbook.docx` (supplied outside this
  project folder; the project folder did not contain a starter kit).

## Non-negotiable rules

1. Do not delete, skip, or weaken an existing test to make the suite pass.
2. Do not change an expected value merely to match buggy output.
3. Do not publish to GitHub, run `git init`, stage, commit, push, or rewrite
   history. The student handles Git manually.
4. Do not fabricate command output, measurements, or student reflections.
5. Keep the implementation beginner-readable: descriptive names, small
   methods, comments only where the reasoning is not obvious.
6. `StudentDirectory` must never expose its mutable backing map.
7. Duplicate student IDs must not overwrite an existing record.
8. Tests must run with assertions enabled: `java -ea -cp out LabTestRunner`.
9. Any AI-assisted work must be described honestly in `AGENT_LOG.md`.

## Layout

```
src/    production classes and workbook demonstrations
test/   acceptance runner and student tests
out/    compiled classes (ignored, never submitted)
```

## Commands

Windows:

```bat
run-tests.bat
```

Cross-platform:

```bash
javac -d out src/*.java test/*.java
java -ea -cp out LabTestRunner
```
