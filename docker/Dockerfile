FROM tomcat:9-jdk11

# Copy WAR file to Tomcat webapps directory
COPY geworkbench.war /usr/local/tomcat/webapps/

# Set up a volume for persistent data (e.g., logs or uploads)
VOLUME ["/usr/local/tomcat/geworkbench", "/usr/local/tomcat/public_annotations"]

# Expose port 8080
EXPOSE 8080

# Start Tomcat when the container launches
CMD ["catalina.sh", "run"]