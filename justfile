dir-git-of-theseus := ".git-of-theseus"

default:
    @just --list

git-of-theseus:
    git-of-theseus-analyze --interval=86400 . --outdir={{dir-git-of-theseus}}
    git-of-theseus-stack-plot {{dir-git-of-theseus}}/dirs.json --outfile={{dir-git-of-theseus}}/dirs.png
    git-of-theseus-stack-plot {{dir-git-of-theseus}}/cohorts.json --outfile={{dir-git-of-theseus}}/years.png

# Build release app with Compose Compiler metrics/reports enabled
compose-metrics:
    ./gradlew assembleRelease -Phabittracker.enableComposeCompilerReports=true

# Run license report task and update license report asset in app
update-licenses:
    ./gradlew licenseeRelease --no-configuration-cache
    cp app/build/reports/licensee/release/artifacts.json app/src/main/assets/licenses.json