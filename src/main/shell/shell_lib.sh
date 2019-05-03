#!/usr/bin/env bash
#平常会用到的一些Shell函数，在此总结下，方便以后复用
#会持续更新
#find BIN_HOME and APP_HOME
function findBinHome() {
    SHELL_NAME=${0}
    BIN_HOME=$(dirname "${0}")
    #BIN_HOME path
    BIN_HOME=$(cd "${BIN_HOME}";pwd)
    #APP_HOME path
    APP_HOME=$(dirname "${BIN_HOME}")
}