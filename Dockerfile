FROM tomcat:latest

#Setup tomcat server
RUN sed -i 's/port="8080"/port="8083"/' ${CATALINA_HOME}/conf/server.xml
RUN mv webapps webapps2
RUN mv webapps.dist/ webapps
RUN rm -f ${CATALINA_HOME}/conf/tomcat-users.xml
RUN rm -f ${CATALINA_HOME}/webapps/manager/META-INF/context.xml

#Copying file configuration
COPY tomcat-users.xml ${CATALINA_HOME}/conf/
COPY context.xml ${CATALINA_HOME}/webapps/manager/META-INF/
COPY target/*.war ${CATALINA_HOME}/webapps/

CMD [ "catalina.sh", "run" ]