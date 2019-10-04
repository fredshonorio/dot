#!/bin/bash
export BORG_PASSPHRASE="once found but now lost"
USER=fred
NAME=vault-warrant
ARCHIVE=/home/$USER/Tresors/$NAME
MOUNT=/home/$USER/borg-$NAME
LAST=$(borg list $ARCHIVE --last 1 --format="{archive}")
MNT_PREFIX=$MOUNT/home/$USER

mkdir -p $MOUNT
borg mount $ARCHIVE::$LAST $MOUNT

mkdir -p ~/.ssh/    && meld $MNT_PREFIX/.ssh    ~/.ssh
mkdir -p ~/.gradle/ && meld $MNT_PREFIX/.gradle ~/.gradle
mkdir -p ~/.aws/    && meld $MNT_PREFIX/.aws    ~/.aws

borg umount $MOUNT
# borg diff $ARCHIVE::$LAST ~/.ssh
