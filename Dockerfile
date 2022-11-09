FROM tomcat:latest
RUN sed -i 's/port="8080"/port="8082"/' /urs/local/tomcat/conf/server.xml
ADD target/*.war /urs/local/tomcat/webapps/
EXPOSE 8083
CMD [ "catalina.sh", "run" ]