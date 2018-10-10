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
