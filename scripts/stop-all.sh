#!/usr/bin/env bash
# Stops services started by start-all.sh
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for pidfile in "$ROOT"/logs/*.pid; do
  [ -f "$pidfile" ] || continue
  pid=$(cat "$pidfile")
  kill "$pid" 2>/dev/null && echo "stopped $(basename "$pidfile" .pid) ($pid)"
  rm -f "$pidfile"
done
