back dirs up automatically (systemd)
$ borg init -e repokey --make-parent-dirs /home/fred/Tresors/vault-warrant/
-- don't forget to add to tresorit

# Create a script to automate
- template run.sh
- creation of /opt/my-borg-backup, chown, chmod 700
- borg init

--
make scripts for easy diff, restore
