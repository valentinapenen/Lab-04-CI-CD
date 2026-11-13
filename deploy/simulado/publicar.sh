#!/bin/sh
set -eu

set -- target/*.jar
if [ "$#" -ne 1 ] || [ ! -f "$1" ]; then
    echo 'Se esperaba un único JAR en target/.' >&2
    exit 1
fi

versiones="${JENKINS_HOME:?}/lab04-simulado/releases"
estado="$JENKINS_HOME/lab04-simulado/status"
if [ ! -d "$versiones" ] || [ ! -d "$estado" ]; then
    echo 'Primero hay que preparar el ambiente simulado según el README.' >&2
    exit 1
fi

revision=$(git rev-parse HEAD)
jar="app-${BUILD_NUMBER:?}-$revision.jar"
anterior=$(readlink "$versiones/current.jar" || true)

activar() {
    ln -sfn "$1" "$versiones/current.tmp"
    mv -Tf "$versiones/current.tmp" "$versiones/current.jar"
}

cp "$1" "$versiones/$jar.tmp"
mv -f "$versiones/$jar.tmp" "$versiones/$jar"
activar "$jar"
echo "Publicado $jar. Esperando HTTP 200 de esta versión."

intento=0
while [ "$intento" -lt 60 ]; do
    version_lista=$(cat "$estado/ready" 2>/dev/null || true)
    if [ "$version_lista" = "$jar" ]; then
        echo "Despliegue confirmado: HTTP 200 para $revision"
        find "$versiones" -maxdepth 1 -type f -name 'app-*.jar' ! -name "$jar" ! -name "$anterior" -delete
        exit 0
    fi
    intento=$((intento + 1))
    sleep 2
done

if [ -n "$anterior" ] && [ "$anterior" != "$jar" ]; then
    activar "$anterior"
    echo "Se volvió a seleccionar $anterior."
fi
echo "La versión $revision no respondió HTTP 200 a tiempo." >&2
exit 1
