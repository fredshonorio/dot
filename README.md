This app helps to bootstrap my manjaro machines and keep their
configuration more or less synchronized.

## Usage

Requires sbt:
`$ sudo pacman -S sbt`

Everything else is installed by the script itself. In this directory run `$ sbt run`

## Manual intervention

This is a list of things that have to be done manually, for now:

/home/fred/.sbt/.s3credentials

- enable smb, nmb
- merge /etc/samba/smb.conf ???
- setup firefox sync
- setup chamber, aws-vault with file backend
- setup borg backup script
- uninstall some apps (steam-manjaro, libreoffice-still)
- remove some xfce keybinds
- replace xfwm4 with  xmonad
  - disable super key binding
  - disable xfce startup stuff
    - xfce4-panel -> never
    - thunar -> never
    - xfwm4 -> never
  - uninstall xfdesktop


add color pacman
https://github.com/Jguer/yay/issues/123
