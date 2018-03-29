# package file of sofa-ark-all.jar

# cd work directory
cd $1

# cd project root directory and package sofa-ark-all
mvn clean package -DskipTest=true -Duser.dir=$2

