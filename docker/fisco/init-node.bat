@echo off
REM FISCO BCOS 节点初始化脚本 (Windows)
REM 使用 Docker 容器生成节点配置

echo ==========================================
echo FISCO BCOS 节点初始化脚本
echo ==========================================

REM 检查是否已存在节点配置
if exist "nodes\127.0.0.1\node0" (
    echo 节点配置已存在，跳过生成
    echo 如需重新生成，请先删除 nodes 目录
    goto :end
)

echo 使用 Docker 生成节点配置...

REM 创建临时目录
if not exist "nodes" mkdir nodes

REM 使用 Docker 运行 build_chain.sh
docker run --rm -v "%cd%\nodes:/nodes" fiscoorg/fiscobcos:v2.9.1 /bin/bash -c "cd /tmp && curl -#LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.9.1/build_chain.sh && chmod +x build_chain.sh && bash build_chain.sh -l 127.0.0.1:1 -p 30300,20200,8545 -o /nodes && chown -R 1000:1000 /nodes"

if %errorlevel% neq 0 (
    echo 节点配置生成失败！
    goto :end
)

echo ==========================================
echo 节点配置生成完成！
echo ==========================================

echo.
echo 生成的文件:
dir /b nodes\127.0.0.1\node0\

:end
