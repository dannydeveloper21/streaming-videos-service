FROM tomcat:latest
ADD target/*.war /urs/local/apache-tomcat-10.0.23/webapps/
EXPOSE 8082
CMD [ "catalina.sh", "run" ]