# -*- mode: sh -*-

# editor
export EDITOR=emacs
export VISUAL=emacs
export SUDO_EDITOR="emacs --no-window-system"
export AWS_VAULT_BACKEND=file
export SSH_COMMAND=sshrc

export PATH="$HOME/bin:$PATH"

alias ap='readlink -e "$1"'                                                                  # Absolute path of the argument path
alias e='emacsclient -c'                                                                     # emacs (connects to emacs server)
alias ed='emacs --daemon'                                                                    # start emacs server
alias ek='emacsclient -e "(kill-emacs)"'                                                     # kill emacs server
alias es='emacs'                                                                             # emacs solo (doesn't connect to emacs server)
alias et='emacsclient -t'                                                                    # ecmas inside the terminal emulator
alias m-st='emacsclient -c --eval "(progn (magit-status) (delete-other-windows))"'           # (ma)git status
alias m-log='emacsclient -c --eval "(progn (magit-log-branches) (delete-other-windows))"'    # (ma)git log
alias ducks='for i in G M K; do du -ah | grep "[0-9]$i" | sort -nr -k 1; done | head -n 11'  # top 10 largest files/directories
alias t='(sakura -d $PWD) &> /dev/null &'                                                    # spawn another terminal in this directory
# export _Z_EXCLUDE_DIRS=('/home/fred/Downloads' '/home/fred/z')

# tar a directory <x> named <x>.tgz
function tardir() {
    tar -czvf "$1".tgz "$1"
}

function dbash() {
    docker exec -it $1 bash
}

# no args
function xmonad_watch() {
    echo ".xmonad/xmonad.hs" | entr -s 'xmonad --recompile'
}

# 1 arg
function hs_watch() {
    echo "$1" | entr -s "stack ghc $1 -- -Wall"
}

export QT_QPA_PLATFORMTHEME="qt5ct"
export GTK2_RC_FILES="$HOME/.gtkrc-2.0"
