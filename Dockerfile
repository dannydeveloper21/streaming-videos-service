FROM tomcat:latest
ADD target/*.war ${CATALINA_HOME}/webapps/
CMD [ "catalina.sh", "run" ]