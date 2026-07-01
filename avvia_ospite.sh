#!/bin/bash
# Avvia il client AFAM come Soggetto Esterno (ospite). Il server deve essere già
# in esecuzione (vedi avvia.sh).
#
# Uso:
#   ./avvia_ospite.sh                 -> apre la ricerca dei profili pubblici
#   ./avvia_ospite.sh <idLink>        -> apre direttamente il portfolio condiviso
#
# Nota: si usa l'esecuzione "ospite" del plugin (javafx:run@ospite), che imposta
# mainClass=ClientOspiteMain. L'id del link e l'URI del server vengono passati
# come argomenti dell'applicazione via -Djavafx.args (una JVM separata non
# eredita le -D dalla riga di comando di Maven).
set -euo pipefail

MVN="/Users/cristianingrao/afam/apache-maven-3.9.6/bin/mvn"
DIR="$(cd "$(dirname "$0")" && pwd)"
ID_LINK="${1:-}"

SERVER_URI="$(cat /tmp/afam_server_uri.txt 2>/dev/null | tr -d '\n')"
[ -n "$SERVER_URI" ] || SERVER_URI="http://localhost:8080/api/"

echo "Avvio client ospite AFAM..."
"$MVN" -f "$DIR/pom.xml" javafx:run@ospite -Djavafx.args="${ID_LINK} ${SERVER_URI}"
