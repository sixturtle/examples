# Running in Wildfly Container

    $ export JBOSS_HOME=/opt/wildfly
    $ gradle clean build deploy
    $ docker run -d -p 8080:8080 -v /opt/wildfly:/opt/dist/wildfly --name wildfly sixturtle/wildfly-ex

