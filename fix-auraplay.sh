#!/bin/bash
cd AuraPlay

# Delete ALL old source files
# rm -rf app/src/main/java/com/auraplay/player/*

# Copy new files (the workspace already has them)
# After copying, do:
git add -A
git commit -m "Clean slate — fresh code only"
git tag -d v1.0.0 2>/dev/null
git push origin :refs/tags/v1.0.0 2>/dev/null
git push origin main --force
git tag v1.0.0
git push origin v1.0.0
