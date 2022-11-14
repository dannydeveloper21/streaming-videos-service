FROM tomcat:latest
ARG CATALINA_HOME=/usr/local/apache-tomcat-10.0.23
RUN sed -i 's/port="8080"/port="8083"/' ${CATALINA_HOME}/conf/server.xml
ADD target/*.war ${CATALINA_HOME}/webapps/
CMD [ "catalina.sh", "run" ]