#!/bin/bash
# Run StudyBuddy with Java 17

export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java: $JAVA_HOME"
echo "Starting StudyBuddy..."
echo ""

cd "$(dirname "$0")"
mvn spring-boot:run

