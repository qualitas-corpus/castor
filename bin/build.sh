#!/bin/sh

# $Id: build.sh 8139 2009-03-26 21:45:42Z wguttmn $

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java

DIRNAME=`dirname $0`
CASTOR_HOME=`cd $DIRNAME/..; pwd`
BIN_LIB_D=$CASTOR_HOME/bin/lib
LIB_D=$CASTOR_HOME/lib
BUILD_D=$CASTOR_HOME/build
SRC_D=$CASTOR_HOME/src

# Ant infrastructure
CLASSPATH=$BIN_LIB_D/ant-1.6.jar
CLASSPATH=$CLASSPATH:$BIN_LIB_D/ant-1.6-launcher.jar
CLASSPATH=$CLASSPATH:$BIN_LIB_D/ant-1.6-trax.jar
CLASSPATH=$CLASSPATH:$BIN_LIB_D/ant-junit-1.6.jar
# JUnit
CLASSPATH=$CLASSPATH:$LIB_D/junit-4.5.jar
# Build artefacts
CLASSPATH=$CLASSPATH:$BUILD_D/classes
# Sun's tools.jar, required for compilation
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
# Various 3rd party deps
CLASSPATH=$CLASSPATH:$LIB_D/xerces-J_1.4.0.jar:$LIB_D/commons-logging-1.1.jar

for i in `ls $LIB_D/*.jar`
   do 
       CLASSPATH=$CLASSPATH:$i
   done

$JAVA -classpath $CLASSPATH -Dant.home=lib org.apache.tools.ant.Main "$@" -buildfile $SRC_D/build.xml
