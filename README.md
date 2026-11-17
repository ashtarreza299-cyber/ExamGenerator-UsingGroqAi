# ExamGUI

A Swing desktop application that detects questions from a Microsoft Word file
(using the Perplexity AI API — Decider Flash V1.3 — when online, with an offline tagging fallback),
classifies them by type (multiple-choice, true/false, blank-space, descriptive),
lets you configure an exam structure, and generates shuffled exam papers —
a different paper for each student.

## Project Structure

```text
ExamGUI/
├── pom.xml                          # Maven build (dependencies: Apache POI, Jackson)
├── src/
│   └── main/
│       ├── java/org/example/
│       │   ├── Main.java            # ENTRY POINT: builds the JFrame, wires panels & model
│       │   │
│       │   ├── model/               # Pure data & state (no Swing dependencies)
│       │   │   ├── Question.java                 # Abstract question base class
│       │   │   ├── MultipleChoiceQuestion.java   # MCQ + options/shuffleOption()
│       │   │   ├── TrueFalseQuestion.java
│       │   │   ├── BlankSpaceQuestion.java
│       │   │   ├── DescriptiveQuestion.java
│       │   │   ├── ExamStructure.java            # Counts per question type
│       │   │   ├── Model.java                    # Shared app state (observer subject)
│       │   │   └── ModelListener.java            # Observer interface for state changes
│       │   │
│       │   ├── service/             # Business logic: parsing, AI, generation
│       │   │   ├── QuestionParser.java           # Parses .docx into Question objects
│       │   │   ├── QuestionExtractor.java        # Online (Perplexity) / offline extraction
│       │   │   ├── QuestionClassifier.java       # Offline heuristic classification
│       │   │   ├── PerplexityClassifier.java    # Perplexity API HTTP client
│       │   │   ├── ExamStructureParser.java      # Parses exam-structure config
│       │   │   ├── ExamBuilder.java              # Ordered exam list + shuffling rules
│       │   │   ├── ExamGenerator.java            # Writes generated exam files (POI)
│       │   │   ├── GenerateExam.java             # Alternate DOCX generator (see notes)
│       │   │   └── QuestionShuffler.java         # Standalone shuffler (see notes)
│       │   │
│       │   ├── view/                # Swing UI (panels/screens)
│       │   │   ├── ProgramViewPanel.java         # Base panel with model binding
│       │   │   ├── SideBarPanel.java             # Status sidebar (ModelListener)
│       │   │   ├── DashBoardPanel.java           # Main wizard container
│       │   │   ├── DashBoardInnerPanel.java      # Interface for wizard steps
│       │   │   ├── LoadQuestionPanel.java        # Step: load & parse .docx
│       │   │   ├── ExamSettingPanel.java         # Step: exam settings
│       │   │   ├── QuestionTypePanel.java        # Step: question-type selection
│       │   │   ├── OutputSetting.java            # Step: output folder/options
│       │   │   ├── QuestionDistributionPanel.java# Distribution summary UI
│       │   │   ├── QuestionPreviewPanel.java     # Preview container
│       │   │   └── QuestionPreviewer.java        # Renders individual questions
│       │   │
│       │   ├── view/component/      # Reusable custom Swing widgets
│       │   │   ├── ExamComponent.java            # Common styling interface
│       │   │   ├── MyButton.java / MyCheckBox.java / ToggleSwitch.java
│       │   │   ├── NumberSpinner.java
│       │   │   ├── RoundedField.java / RoundedFileChooserField.java / RoundedPanel.java
│       │   │   └── QuestionGraph.java            # Distribution bar chart widget
│       │   │
│       │   └── config/              # Configuration & environment helpers
│       │       ├── ApiKeyManager.java            # GROQ_API_KEY storage (~/.examgenerator)
│       │       └── NetworkUtils.java             # Connectivity check
│       │
│       └── resources/
│           └── Image/               # Icons loaded via "/Image/..." classpath paths
└── README-original.md               # Original author's notes (preserved)
```

## Architecture Decisions

- **model / view / service / config separation (SoC):** Domain classes previously
  sat in one flat package alongside Swing widgets. `model` now has zero Swing/UI
  imports; `service` contains all parsing/AI/generation logic; `view` depends on
  model+service, never the reverse direction into GUI internals.
- **`view/component` sub-package:** Small reusable widgets are separated from
  full screens/panels to keep the top-level view package readable.
- **`Main.buildExam` → `service.ExamBuilder`:** Static business logic was moved
  out of the JFrame subclass (SRP). Behavior is unchanged (moved verbatim).
- **Dead code preserved:** `QuestionShuffler` and `GenerateExam` are not
  referenced anywhere, but were kept (moved to `service/`) rather than deleted,
  as agreed. They appear to be earlier drafts of `Main`/`DashBoardPanel` logic.
- **Known pre-existing issues (NOT changed, behavior preserved):**
  - `QuestionTypePanel` loads images from `/org/example/Image/QuestionType/...`
    which does not exist in resources (broken before this refactor).
  - The Windows Look-and-Feel only applies on Windows.
  - `buildExam`/`ExamBuilder.buildExam` currently has no callers.

## Build & Run

Requires JDK 17+ and Maven.

```bash
mvn compile        # build
mvn exec:java -Dexec.mainClass=org.example.Main   # run (or run org.example.Main from your IDE)
```

Set the Perplexity API key when prompted at first launch (stored in
`~/.examgenerator/config.properties`), or export `GROQ_API_KEY`.

## Adding New Classes

| New class type                              | Put it in                  |
|---------------------------------------------|----------------------------|
| Data/domain object or shared state          | `org.example.model`        |
| Parsing, AI, file generation, business rule | `org.example.service`      |
| Full screen / panel                         | `org.example.view`         |
| Reusable small widget                       | `org.example.view.component` |
| Configuration / environment helper          | `org.example.config`       |

Never import `org.example.view.*` from `model` or `config`; keep `service`
free of Swing panel references where possible.
