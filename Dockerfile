# syntax=docker/dockerfile:1
# OpenRune server for running 24/7 on a Linux box.
# Build + run:  docker compose up -d --build      Logs:  docker compose logs -f
#
# The build follows .github/workflows/ci.yml and release-server.yml: download and build the cache,
# merge plugin gamevals, build server.jar. Like the upstream test server, the runtime image only
# needs server.jar, game.yml and .data/{cache,gamevals,gamevals-binary}.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew \
 && cp game.example.yml game.yml \
 && sed -i 's|^    pool-size: 10$|    pool-size: 10\n    embedded-pgdata-dir: "/srv/openrune/state/postgres"|' game.yml \
 && grep -q 'embedded-pgdata-dir: "/srv/openrune/state/postgres"' game.yml \
 && ./gradlew :or-cache:freshCache :or-cache:mergePluginGamevals --no-daemon --console=plain \
 && ./gradlew :or-cache:buildCache :or-cache:mergePluginGamevals generateRsa :server:app:shadowJar \
        --no-daemon --console=plain

FROM eclipse-temurin:21-jre
# Embedded Postgres refuses to run as root.
RUN useradd --create-home --shell /usr/sbin/nologin openrune \
 && mkdir -p /srv/openrune/.data /srv/openrune/state /srv/openrune/default-keys \
 && chown -R openrune:openrune /srv/openrune
WORKDIR /srv/openrune
COPY --from=build --chown=openrune:openrune /src/server/app/build/libs/server.jar /src/game.yml ./
COPY --from=build --chown=openrune:openrune /src/.data/cache .data/cache
COPY --from=build --chown=openrune:openrune /src/.data/gamevals .data/gamevals
COPY --from=build --chown=openrune:openrune /src/.data/gamevals-binary .data/gamevals-binary
COPY --from=build --chown=openrune:openrune /src/.data/game.key /src/.data/client.key default-keys/
COPY --chmod=755 docker/entrypoint.sh /usr/local/bin/openrune-entrypoint
USER openrune
EXPOSE 43594
ENTRYPOINT ["openrune-entrypoint"]
CMD ["java", "-XX:AutoBoxCacheMax=65535", "-Xms512m", "-Xmx4g", "-jar", "server.jar"]
