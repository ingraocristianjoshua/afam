#!/bin/bash
# Avvia il client AFAM come Soggetto Esterno (ospite).
# Il server deve essere già in esecuzione.
MVN="/Users/cristianingrao/afam/apache-maven-3.9.6/bin/mvn"
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Avvio client ospite AFAM..."
"$MVN" -f "$DIR/pom.xml" javafx:run -Djavafx.mainClass=com.afam.client.ClientOspiteMain
