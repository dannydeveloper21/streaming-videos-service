FROM tomcat:latest
ADD target/*.war /urs/local/apache-tomcat-10.0.23/webapps/
EXPOSE 8083
CMD [ "catalina.sh", "run" ]