#!/bin/bash
# Avvia il server AFAM e, quando è pronto, il client JavaFX (utente autenticato).
#
# Uso:
#   ./avvia.sh                       -> tutto in locale (http://localhost:8080)
#   ./avvia.sh https://xxx.ngrok.app -> espone i link di condivisione via ngrok
set -euo pipefail

MVN="/Users/cristianingrao/afam/apache-maven-3.9.6/bin/mvn"
DIR="$(cd "$(dirname "$0")" && pwd)"
NGROK_URL="${1:-}"

if [ -n "$NGROK_URL" ]; then
    BASE="${NGROK_URL%/}"
    SERVER_URI="${BASE}/api/"
    LINK_BASE_URL="${BASE}/api/share/"
    echo "Modalità ngrok attiva"
    echo "  Server URI : $SERVER_URI"
    echo "  Link base  : $LINK_BASE_URL"
else
    SERVER_URI="http://localhost:8080/api/"
    LINK_BASE_URL="http://localhost:8080/api/share/"
fi
# Reso disponibile all'handler afam:// (apertura portfolio da link)
echo "$SERVER_URI" > /tmp/afam_server_uri.txt

echo "Avvio server AFAM..."
"$MVN" -f "$DIR/pom.xml" exec:java -Dlink.baseUrl="$LINK_BASE_URL" &
SERVER_PID=$!
# Ferma il server quando lo script termina o viene interrotto
trap 'kill "$SERVER_PID" 2>/dev/null; rm -f /tmp/afam_server_uri.txt' EXIT

echo "Attendo che il server sia pronto..."
until curl -s http://localhost:8080/api/pubblico/ospite > /dev/null 2>&1; do
    sleep 1
done

echo "Server pronto. Avvio client JavaFX..."
"$MVN" -f "$DIR/pom.xml" javafx:run -Dserver.baseUri="$SERVER_URI"
