#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/target/classes"
rm -rf "$OUT"
mkdir -p "$OUT"
find "$ROOT/src/main/java" -name '*.java' -print0 | xargs -0 javac --release 17 -d "$OUT"
java -cp "$OUT" kz.edu.assignment6.demo.DemoApplication
