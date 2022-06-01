#!/usr/bin

projectBaseDir=$1
mvn_options=$2

cd $projectBaseDir

# 执行编译，生成BizFatJar
mvn -B -U package -Dmaven.test.skip=true -DskipTests=true $mvn_options > $projectBaseDir/biz_log.txt