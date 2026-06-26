#!/bin/bash
MVN="/Users/cristianingrao/afam/apache-maven-3.9.6/bin/mvn"
DIR="$(cd "$(dirname "$0")" && pwd)"

# Uso: ./avvia.sh [URL_NGROK]
# Esempio: ./avvia.sh https://abc123.ngrok-free.app
NGROK_URL="$1"

if [ -n "$NGROK_URL" ]; then
    BASE="${NGROK_URL%/}"
    SERVER_URI="${BASE}/api/"
    LINK_BASE_URL="${BASE}/api/share/"
    echo "$SERVER_URI" > /tmp/afam_server_uri.txt
    echo "Modalità ngrok attiva"
    echo "  Server URI  : $SERVER_URI"
    echo "  Link base   : $LINK_BASE_URL"
else
    SERVER_URI="http://localhost:8080/api/"
    LINK_BASE_URL="http://localhost:8080/api/share/"
    echo "$SERVER_URI" > /tmp/afam_server_uri.txt
fi

echo "Avvio server AFAM..."
"$MVN" -f "$DIR/pom.xml" exec:java \
    -Dlink.baseUrl="$LINK_BASE_URL" &
SERVER_PID=$!

echo "Attendo che il server sia pronto..."
until curl -s http://localhost:8080/api/pubblico/ospite > /dev/null 2>&1; do
    sleep 1
done

echo "Server pronto. Avvio client JavaFX..."
"$MVN" -f "$DIR/pom.xml" javafx:run \
    -Dserver.baseUri="$SERVER_URI"

kill $SERVER_PID 2>/dev/null
rm -f /tmp/afam_server_uri.txt
