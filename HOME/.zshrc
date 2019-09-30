# The following lines were added by compinstall
zstyle :compinstall filename '~/.zshrc'

autoload -Uz compinit
compinit
# End of lines added by compinstall
# Lines configured by zsh-newuser-install
HISTFILE=~/.histfile
HISTSIZE=10000
SAVEHIST=10000
# End of lines configured by zsh-newuser-install

zstyle ':prezto:module:terminal' auto-title 'yes'
bindkey '^R' history-incremental-pattern-search-backward
bindkey '^R' history-incremental-search-backward

promptinit
prompt agnoster

ZSH_HIGHLIGHT_HIGHLIGHTERS=(main brackets pattern)

source ~/.profile

[[ -r "/usr/share/z/z.sh" ]] && source /usr/share/z/z.sh

eval "$(direnv hook zsh)"

# This runs before running any command
preexec () {
    CMD_START_DATE=$(date +%s)
    CMD_NAME=$1
}

# This runs when a command stops and relinquishes the prompt
precmd () {
    if ! [[ -z $CMD_START_DATE ]]; then
        local CMD_END_DATE=$(date +%s)
        local CMD_ELAPSED_TIME=$(($CMD_END_DATE - $CMD_START_DATE))
        local CMD_NOTIFY_THRESHOLD=10

        if [[ $CMD_ELAPSED_TIME -gt $CMD_NOTIFY_THRESHOLD ]]; then
            notify-send 'Job finished' "The job \"$CMD_NAME\" has finished in $CMD_ELAPSED_TIME seconds."
        fi
    fi
    # this avoids running the previous statements when the user presses Ctrl+C/Enter on the shell
    # In that case precmd is invoked but preexec is not, so we would get a notification as if the previous
    # command had stopped at the point of the Ctrl+C/Enter
    CMD_START_DATE=""
}

autoload bashcompinit
bashcompinit
. /opt/asdf-vm/completions/asdf.bash
. /opt/asdf-vm/asdf.sh
