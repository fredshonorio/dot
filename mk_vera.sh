#!/bin/sh

# A script to create small veracrypt volumes, for secrets

set -ex

SIZE=2M
NAME=${1}
KEYFILE=""  # none
PIM=0       # no pim (i think)
TYPE=normal # not hidden
FS=ext4
ENCR=AES
HASH=sha512

echo "> Creating volume"

veracrypt -t \
  --create      $NAME \
  --hash        $HASH \
  --encryption  $ENCR \
  --filesystem  $FS   \
  --volume-type $TYPE \
  --size        $SIZE \
  --pim         $PIM  \
  -k "$KEYFILE"

echo "> Mounting volume to chown it"

TMP=$(mktemp -d)
veracrypt --mount $NAME $TMP

sudo chown -R $USER $TMP/

veracrypt --dismount $NAME

echo "> Done!"
