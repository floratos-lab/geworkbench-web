# geworkbench-web
geWorkbench platform on the Web

This is a standard Java web application with backend database. To build, you need Java, and maven. To deploy, you need a Java application container, e.g. tomcat (tested), and a database system that supports JDBC, e.g. MySQL (tested).

## developers's notes

### Java version

The project needs to be built using Java 8. Although the application may be deployed and work under a newer Java, e.g. Java 11, the building must be done using Java 8 for now.

### typical testing procedure

1. build `mvn clean && mvn package`
    - before building, config files `src/main/resources/application.properties` and `src/main/resources/META-INF/persistence.xml` need to be created
    - the database specified needs to be available
2. deploy `sudo cp target/geworkbench.war ${CATALINA_HOME}/webapps`
   - assuming the tomcat server is running at `${CATALINA_HOME}/`
3. test `sudo tail -f ${CATALINA_HOME}/logs/catalina.out`. Check from the browser at `http://localhost:8080/geworkbench/`

### production deployment plan
1. backup the current deployment
    + backup the webapps subdirectory and the war file (this will help diagnose any issues following the deployment or an emergency rollback if anything goes wrong during the deployment).
    + backup the backend database as a sql dump
2. check out the code to be deployed, for a certain tag (a branch if we expect active development continues separately for the deployed code from the master branch)
3. update the properties files that are not in the github, typically by copying from a specified location. This should include `src/main/resources/application.properties` and `src/main/resources/META-INF/persistence.xml`
4. build the war file by execute `mvn package`
5. copy the war file to the destination machine, at `$CATALINA_HOME/webapps/`
6. restart tomcat if necessary

### functionality dependency
1. a few web services deployed separated. They might be on other servers, URLs specified `application.properties`.
    + ANOVA analysis, t-test analysis, hierarchical clustering analysis, ARACNe analysis, MS-Viper analysis, PBQDI analysis
2. gmail email support for the purpose of account registration communication
3. LINCS web application. For our production deployment, this is on the same tomcat.

Other dependencies, e.g. CNKB query servlet, are indirect via those listed above.

### technology dependency
- [cystoscape.js](http://js.cytoscape.org/)
