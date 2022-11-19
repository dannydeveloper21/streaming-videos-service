FROM tomcat:9.0.69-jdk8-corretto
LABEL maintainer="php7mvc.developers@gmail.com"

#Setup tomcat server
RUN sed -i 's/port="8080"/port="8083"/' ${CATALINA_HOME}/conf/server.xml
RUN mv webapps webapps2
RUN mv webapps.dist/ webapps

#Removing configuration files
RUN rm -f ${CATALINA_HOME}/conf/tomcat-users.xml
RUN rm -f ${CATALINA_HOME}/webapps/manager/META-INF/context.xml

#Copying configuration files
COPY tomcat-users.xml ${CATALINA_HOME}/conf/
COPY context.xml ${CATALINA_HOME}/webapps/manager/META-INF/
ADD target/*.war ${CATALINA_HOME}/webapps/

EXPOSE 8083
CMD [ "catalina.sh", "run" ]