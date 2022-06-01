#!/usr/bin

projectBaseDir=$1
mvn_options=$2
gitAddress=$3
branch=$4
projectName=$5

cd $projectBaseDir

# 确保之前不存在
rm -rf $projectName

# 获取基座项目
git clone -b $branch  $gitAddress

cd $projectName

# 执行编译，生成masterFatJar
mvn -B -U clean install  -Dmaven.test.skip=false -DskipTests=false $mvn_options > $projectBaseDir/master_log.txt