#!/usr/bin/env bash
# Starts every HealthHarbor backend service from the packaged jars (run `mvn -DskipTests package` first).
# Usage: scripts/start-all.sh [spring-profile]     e.g. scripts/start-all.sh h2   (embedded DB, no PostgreSQL needed)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROFILE="${1:-}"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"
JAVA_OPTS="${JAVA_OPTS:--Xms64m -Xmx256m -XX:+UseSerialGC -XX:TieredStopAtLevel=1}"
PROFILE_ARG=""
[ -n "$PROFILE" ] && PROFILE_ARG="--spring.profiles.active=$PROFILE"

start() {
  local name=$1
  local jar
  jar=$(ls "$ROOT/$name"/target/"$name"-*.jar 2>/dev/null | grep -v original | head -1 || true)
  if [ -z "$jar" ]; then echo "!! $name jar not found - run: mvn -DskipTests package"; exit 1; fi
  (cd "$ROOT/$name" && nohup java $JAVA_OPTS -jar "$jar" $PROFILE_ARG > "$LOG_DIR/$name.log" 2>&1 & echo $! > "$LOG_DIR/$name.pid")
  echo "started $name (pid $(cat "$LOG_DIR/$name.pid"))"
}

wait_for() {
  local url=$1 name=$2
  for _ in $(seq 1 90); do
    if curl -fs "$url" > /dev/null 2>&1; then echo "  $name is up"; return 0; fi
    sleep 2
  done
  echo "  $name did not become healthy - see $LOG_DIR/$name.log"; return 1
}

start discovery-server
wait_for http://localhost:8761/actuator/health discovery-server
for svc in auth-service patient-service healthcenter-service appointment-service inventory-service \
           healthcamp-service sanitation-service water-service dashboard-service api-gateway; do
  start "$svc"
done
wait_for http://localhost:8080/actuator/health api-gateway
echo "All services launched. Gateway: http://localhost:8080  Eureka: http://localhost:8761  Logs: $LOG_DIR"
