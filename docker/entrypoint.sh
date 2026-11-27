#!/bin/sh
# Keeps the RSA keys and the database in the state volume, so rebuilding the image doesn't log
# everyone out of their patched clients or wipe characters. First start: the keys generated
# during the build are saved there and used from then on.
set -eu
cd /srv/openrune
for key in game.key client.key; do
    [ -f "state/$key" ] || cp "default-keys/$key" "state/$key"
    cp "state/$key" ".data/$key"
done
exec "$@"
