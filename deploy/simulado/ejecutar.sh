#!/bin/sh
set -eu

jar_en_ejecucion=''
pid_app=''

detener_app() {
    if [ -n "$pid_app" ]; then
        kill "$pid_app" 2>/dev/null || true
        wait "$pid_app" 2>/dev/null || true
    fi
}

trap 'detener_app; exit 0' INT TERM
rm -f /estado/ready

while true; do
    jar_publicado=$(readlink /versiones/current.jar || true)
    if [ -n "$jar_publicado" ] && [ "$jar_publicado" != "$jar_en_ejecucion" ]; then
        rm -f /estado/ready
        detener_app
        echo "Iniciando $jar_publicado"
        java -jar "/versiones/$jar_publicado" &
        pid_app=$!
        jar_en_ejecucion=$jar_publicado
    fi

    if [ -n "$pid_app" ] && ! kill -0 "$pid_app" 2>/dev/null; then
        wait "$pid_app" || true
        rm -f /estado/ready
        pid_app=''
    fi

    if [ -n "$pid_app" ] && [ ! -f /estado/ready ]; then
        codigo_http=$(curl --silent --max-time 3 --output /dev/null --write-out '%{http_code}' http://127.0.0.1:8080/ || true)
        if [ "$codigo_http" = '200' ]; then
            printf '%s\n' "$jar_en_ejecucion" > /estado/ready.tmp
            mv -f /estado/ready.tmp /estado/ready
            echo "HTTP 200 para $jar_en_ejecucion"
        fi
    fi
    sleep 2
done
