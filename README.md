# Habit Builder

[![Build Status](https://app.bitrise.io/app/46845b0b84193b80/status.svg?token=SHqpol98nJjn_O5hbFPjIg&branch=main)](https://app.bitrise.io/app/46845b0b84193b80)
[![codecov](https://codecov.io/gh/ofalvai/HabitTracker/branch/main/graph/badge.svg?token=0PGWK5GQ1P)](https://codecov.io/gh/ofalvai/HabitTracker)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B22373%2Fgit%40github.com%3Aofalvai%2FHabitTracker.git.svg?type=small)](https://app.fossa.com/projects/custom%2B22373%2Fgit%40github.com%3Aofalvai%2FHabitTracker.git?ref=badge_small)

TODO Google Play badge

TODO screenshots table

## Motivation

TODO

## Features

- Keep track of your new habits and see your progress
- Statistics: aggregate and per-habit stats
  - Per-habit: completion rate, weekly and overall count, streaks
  - Overall: calendar heatmap, top habits, top days for habits
- Customizable layouts
- All data is persisted on-device in a database

## Tech stack and architecture

- Compose-only UI, no AppCompat dependency
- Persistence using Room and SQLite
- Async operations with Kotlin Coroutines and Flow
- Instrumented tests for persistence and custom SQL queries
- Unit tests for most of the business logic


## Feature roadmap

- Export data to CSV
- Customizable reminder notifications
- More layouts and customization
- More statistics and charts

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