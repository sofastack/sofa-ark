#!/bin/bash

# 查找正在使用8080端口的进程ID
pid=$(lsof -i :8080 | awk 'NR==2 {print $2}')

# 杀死进程
if [[ -n "$pid" ]]; then
    echo "Killing process with PID $pid"
    kill -9 $pid
else
    echo "No process found using port 8080"
fi