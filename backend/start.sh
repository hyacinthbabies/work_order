#!/bin/bash

# 银行工单系统后端启动脚本
# 使用方法: ./start.sh

set -e

MVN_HOME="./.mvn"
MVN_CMD=""

# 检查系统是否安装了Maven
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
    echo "✅ 使用系统Maven"
elif [ -f "$MVN_HOME/bin/mvn" ]; then
    MVN_CMD="$MVN_HOME/bin/mvn"
    echo "✅ 使用本地Maven"
else
    echo "❌ 未找到Maven，正在下载..."
    mkdir -p .mvn
    
    # 尝试多个镜像源
    MIRRORS=(
        "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
        "https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
        "https://mirrors.aliyun.com/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
    )
    
    for url in "${MIRRORS[@]}"; do
        echo "尝试下载: $url"
        if curl -fsSL "$url" | tar -xzf - -C .mvn --strip-components=1 2>/dev/null; then
            MVN_CMD="$MVN_HOME/bin/mvn"
            echo "✅ Maven下载成功"
            break
        fi
    done
    
    if [ -z "$MVN_CMD" ]; then
        echo ""
        echo "❌ Maven下载失败，请手动安装Maven:"
        echo "   macOS: brew install maven"
        echo "   或访问: https://maven.apache.org/download.cgi"
        exit 1
    fi
fi

echo ""
echo "🚀 启动Spring Boot应用..."
echo ""

# 运行Spring Boot
$MVN_CMD spring-boot:run