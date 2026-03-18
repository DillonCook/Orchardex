#!/bin/sh

##############################################################################
##
##  Gradle start up script for POSIX generated for OrchardDex.
##
##############################################################################

APP_HOME=$(cd "${0%/*}" && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -z "$GRADLE_USER_HOME" ] ; then
    GRADLE_USER_HOME="$APP_HOME/.gradle-user-home"
fi

mkdir -p "$GRADLE_USER_HOME"
export GRADLE_USER_HOME

if [ -z "$ANDROID_USER_HOME" ] ; then
    ANDROID_USER_HOME="$APP_HOME/.android-user-home"
fi

mkdir -p "$ANDROID_USER_HOME"
export ANDROID_USER_HOME

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
