#!/usr/bin/env bash

ps aux | grep "io.github.chenfh5.server.ShellServer" | grep -v "grep"
date

ps aux | grep "source_auth" | grep -v "grep"
