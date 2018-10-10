This app helps to bootstrap my manjaro machines and keep their
configuration more or less synchronized.

## Usage

Requires sbt:
`$ sudo pacman -S sbt`

Everything else is installed by the script itself. In this directory run `$ sbt run`

## Manual intervention

This is a list of things that have to be done manually, for now:

- manage .config/autostart/ files
- chmod +x ~/.bin/unpushed
- setup brave sync
- setup firefox sync
- setup chrome sync
- setup tresorit
- setup chamber, aws-vault
- uninstall some apps (steam-manjaro, libreoffice-still)
- replace xfwm4 with  xmonad
  - disable super key binding
  - add xmonad to startup
  - disable xfce startup stuff
    - xfce4-panel -> never
    - thunar -> never
    - xfwm4 -> never
  - uninstall xfdesktop
