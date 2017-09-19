# geworkbench-web
geWorkbench platform on the Web

This is a standard Java web application with backend database. To build, you need Java, and maven. To deploy, you need a Java application container, e.g. tomcat (tested), and a database system that supports JDBC, e.g. MySQL (tested).

### production deployment plan
0. backup the current deployment
    + backup the webapps subdirectoy and the war file (this will help diagnose any issues following the deployment or an emergency rollback if anything goes wrong during the deployment).
    + backup the backend database as a sql dump
1. check out the code to be deployed, for a certain tag (a branch if we expect active development continues separatly for the deployed code from the master branch)
2. update the properties files that are not in the github, typically by copying from a specified location. This should include `src/main/resources/application.properties` and `src/main/resources/META-INF/persistence.xml`
3. build the war file by execute `mvn package`
4. copy the war file to the destination machine, at `$CATALINA_HOME/webapps/`
5. restart tomcat if necessary

### functionality dependency
1. a few web services deployed separated. For our production deployment, these are on afdev
    + ANOVA analysis, t-test analysis, hierarchical clustering analysis, ARACNe analysis, MS-Viper analyais, PBQDI analysis
2. gmail email support for the purpose of account registration communication
3. LINCS web application. For our production deployment, this is on the same tomcat.

Other dependencies, e.g. CNKB query servlet, are indirect via those listed above.

### dependency on other technologies and projects
1. [cystoscape.js](http://js.cytoscape.org/)
