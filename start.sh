#!/bin/bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

if ! pg_isready -q; then
  service postgresql start
fi

cd "$ROOT/backend"
mvn -q -DskipTests spring-boot:run &

cd "$ROOT/frontend"
if [ ! -d node_modules ]; then
  npm install
fi
npm run dev
