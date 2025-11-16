#!/usr/bin/env bash
# Safe helper to initialize git, commit, and push this repo to the given remote.
# Usage: ./git-push.sh <remote-url> [branch-name]

set -euo pipefail

REMOTE_URL=${1:-https://github.com/SivaKatta1010/build-challenge.git}
BRANCH=${2:-main}

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "Working from: $ROOT_DIR"

cd "$ROOT_DIR"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Initializing new git repository..."
  git init
else
  echo "Git repository already initialized."
fi

# Create a basic .gitignore if missing
if [ ! -f .gitignore ]; then
  cat > .gitignore <<'EOF'
# Java
*.class
*.jar
target/
out/
build/
*.log

# IDEs
.idea/
.vscode/
*.iml

# macOS
.DS_Store

# compiled classes for SA001
SA001/java/classes/
PC001/src/main/java/**/bin/
EOF
  echo "Created .gitignore"
fi

echo "Git status (short):"
git status --short || true

echo "Staging all changes..."
git add .

if git diff --cached --quiet; then
  echo "No staged changes to commit."
else
  git commit -m "chore: add PC001 producer-consumer demo and SA001 sales analysis with sample data"
fi

# Ensure branch name
git branch -M "$BRANCH" || true

if git remote get-url origin >/dev/null 2>&1; then
  echo "Remote 'origin' already set to: $(git remote get-url origin)"
else
  echo "Adding remote origin -> $REMOTE_URL"
  git remote add origin "$REMOTE_URL"
fi

echo "Pushing to origin/$BRANCH..."
if git push -u origin "$BRANCH"; then
  echo "Push successful."
else
  echo "Push failed. Attempting to push to a new branch 'intuit-changes' instead."
  git checkout -b intuit-changes
  git push -u origin intuit-changes
  echo "Pushed to origin/intuit-changes."
fi

echo "Done. If authentication failed, ensure your GitHub credentials or SSH keys are configured."
