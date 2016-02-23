#!/bin/bash
set -e

if [ -z "$JBOSS_HOME" ]; then
    JBOSS_HOME=/opt/wildfly
fi
export JBOSS_HOME

if [ -z "$JBOSS_USER" ]; then
    JBOSS_USER=jboss
fi

# Deploy artifacts and overlay customization from a mapped distribution folder
APP_DIST_DIR=/opt/dist/wildfly
if [ -d $APP_DIST_DIR ] && [ "$(ls -A $APP_DIST_DIR)" ]; then
    cp -r $APP_DIST_DIR/* $JBOSS_HOME
fi


# Load Java configuration.
[ -r /etc/java/java.conf ] && . /etc/java/java.conf
export JAVA_HOME

# Load JBoss AS init.d configuration.
if [ -z "$JBOSS_CONF" ]; then
    JBOSS_CONF=$JBOSS_HOME/bin/init.d/wildfly.conf
fi

[ -r "$JBOSS_CONF" ] && . "${JBOSS_CONF}"


# Set defaults.
if [ -z "$JBOSS_BIND_ADDR" ]; then
  JBOSS_BIND_ADDR=0.0.0.0
fi
 
if [ -z "$JBOSS_BIND_ADDR_MGMT" ]; then
  JBOSS_BIND_ADDR_MGMT=0.0.0.0
fi


# Startup mode of wildfly
if [ -z "$JBOSS_MODE" ]; then
    JBOSS_MODE=standalone
fi

# Startup mode script
if [ "$JBOSS_MODE" = "standalone" ]; then
    JBOSS_SCRIPT=$JBOSS_HOME/bin/standalone.sh
    if [ -z "$JBOSS_CONFIG" ]; then
        JBOSS_CONFIG=standalone.xml
    fi
else
    JBOSS_SCRIPT=$JBOSS_HOME/bin/domain.sh
    if [ -z "$JBOSS_DOMAIN_CONFIG" ]; then
        JBOSS_DOMAIN_CONFIG=domain.xml
    fi
    if [ -z "$JBOSS_HOST_CONFIG" ]; then
        JBOSS_HOST_CONFIG=host.xml
    fi
fi


if [ "$JBOSS_MODE" = "standalone" ]; then
    $JBOSS_SCRIPT -Djboss.bind.address=$JBOSS_BIND_ADDR -Djboss.bind.address.management=$JBOSS_BIND_ADDR_MGMT -c $JBOSS_CONFIG 
else 
    $JBOSS_SCRIPT -Djboss.bind.address=$JBOSS_BIND_ADDR -Djboss.bind.address.management=$JBOSS_BIND_ADDR_MGMT --domain-config=$JBOSS_DOMAIN_CONFIG --host-config=$JBOSS_HOST_CONFIG 
fi
