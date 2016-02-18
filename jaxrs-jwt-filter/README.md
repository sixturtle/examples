Introduction
============

This sample program demonstrates steps required to secure a REST API.


How to Build
============
Run following command to get a WAR bundle

    $ gradle clean build

Once build is successful, the artifact can be found at "build/libs".


How to Deploy
=============
1. Copy build artifact to Wildfly deployment folder
    
    $ cp  build/libs/jaxrs-jwt-filter.war $JBOSS_HOME/standalone/deployment
    
2. Start Wildfly server 

    $ $JBOSS_HOME/bin/run.sh

    
How to Test
===========    
1. Obtain JWT token

    $ curl  --data "username=your-username&password=your-password" https://your-jwt-issuer/protocol/openid-connect/token
    
2. Invoke Echo API

    $ curl -H "Authorization: Bearer <jwt_token>" http://127.0.0.1:8080/jaxrs-jwt-filter/api/echo?message=Hello
    
    
