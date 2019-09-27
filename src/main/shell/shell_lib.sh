#!/usr/bin/env bash
#平常会用到的一些Shell函数，在此总结下，方便以后复用
#会持续更新
#find BIN_HOME and APP_HOME
findBinHome() {
    SHELL_NAME=${0}
    BIN_HOME=$(dirname "${0}")
    #BIN_HOME path
    BIN_HOME=$(cd "${BIN_HOME}";pwd)
    #APP_HOME path
    APP_HOME=$(dirname "${BIN_HOME}")
}
#获取文件名，取代basename
#Useage
#$ basename ~/Pictures/Wallpapers/1.jpg
#1.jpg
#$ basename ~/Pictures/Wallpapers/1.jpg .jpg
#1
#$ basename ~/Pictures/Downloads/
#Downloads
basename() {
    # Usage: basename "path" ["suffix"]
    local tmp
    tmp=${1%"${1##*[!/]}"}
    tmp=${tmp##*/}
    tmp=${tmp%"${2/"$tmp"}"}
    printf '%s\n' "${tmp:-/}"
}
#获得目录，取代dirname
dirname() {
    # Usage: dirname "path"
    local tmp=${1:-.}
    [[ ${tmp} != *[!/]* ]] && {
        printf '/\n'
        return
    }
    tmp=${tmp%%"${tmp##*[!/]}"}
    [[ ${tmp} != */* ]] && {
        printf '.\n'
        return
    }
    tmp=${tmp%/*}
    tmp=${tmp%%"${tmp##*[!/]}"}
    printf '%s\n' "${tmp:-/}"
}