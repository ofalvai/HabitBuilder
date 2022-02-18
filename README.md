# Habit Builder

[![Build Status](https://app.bitrise.io/app/46845b0b84193b80/status.svg?token=SHqpol98nJjn_O5hbFPjIg&branch=main)](https://app.bitrise.io/app/46845b0b84193b80)
[![codecov](https://codecov.io/gh/ofalvai/HabitTracker/branch/main/graph/badge.svg?token=0PGWK5GQ1P)](https://codecov.io/gh/ofalvai/HabitTracker)

TODO Google Play badge

TODO screenshots table

## Motivation

TODO

This project is also a testing ground for new technologies/libraries I'd like to test on a moderately complex project.

## Features
- Keep track of your new habits and see your progress
- Statistics: aggregate and per-habit stats
  - Per-habit: completion rate, weekly and overall count, streaks
  - Overall: calendar heatmap, top habits, top days for habits
- Customizable habit list layout, reorderable list
- All data is persisted on-device in a database
- Onboarding hints, habit archive

## 🛠 Built with

- [Compose-only](https://developer.android.com/jetpack/compose) UI:
    - Material You-ish design
    - Light and dark theme
    - Smooth and delightful animations
    - no AppCompat dependency, single Activity
- [Room](https://developer.android.com/training/data-storage/room): for storing data locally
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + [Flow](https://kotlinlang.org/docs/flow.html): for async operations
- Testing:
    - Unit tests for most of the business logic (ViewModels, mappings, etc.)
    - Instrumented tests for DB operations
    - UI tests for some Compose screens
    - Kotlin Flow testing with [Turbine](https://github.com/cashapp/turbine)
- [Showkase](https://github.com/airbnb/Showkase): UI component browser in debug builds
- [Bugnsag](https://www.bugsnag.com/): Crash and error reporting
- Other interesting bits and integrations:
    - [Licensee](https://github.com/cashapp/licensee): 3rd party dependency validation, license report JSON for the Licenses screen
    - [Ruler](https://github.com/spotify/ruler): a Gradle plugin that measures app size and libraries contributing to it
    - [Gradle version catalog](https://github.com/ofalvai/HabitTracker/blob/main/gradle/libs.versions.toml)
    - [Renovate](https://github.com/ofalvai/HabitTracker/issues/10): Automating dependency update PRs (works with Gradle version catalogs!)

## Development

### Useful Gradle tasks

- `analyzeReleaseBundle`: Run Spotify Ruler
- `licenseeRelease`: Run license check, generate `app/build/reports/licensee/release/artifacts.json`, which should be copied over to `assets/licenses.json`
- `jacocoTestReport`: Generate test coverage (for unit tests)

## Development

### Gradle tasks

- `analyzeReleaseBundle`: Run Spotify Ruler

## License

```
   Copyright 2021 Olivér Falvai

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```