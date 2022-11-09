FROM tomcat:latest
RUN sed -i 's/port="8080"/port="8082"/' ${CATALINA_HOME}/conf/server.xml
ADD target/*.war ${CATALINA_HOME}/webapps/
EXPOSE 8083
CMD [ "catalina.sh", "run" ]