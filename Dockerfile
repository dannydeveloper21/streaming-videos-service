FROM tomcat:latest
RUN sed -i 's/port="8080"/port="8083"/' ${CATALINA_HOME}/conf/server.xml
ADD target/*.war ${CATALINA_HOME}/webapps/
CMD [ "catalina.sh", "run" ]