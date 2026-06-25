@echo off
REM 银行工单系统后端启动脚本 (Windows)
REM 使用方法: start.bat

echo 正在检查Maven...

where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo 找到系统Maven
    mvn spring-boot:run
) else (
    echo 未找到Maven，请先安装Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
)