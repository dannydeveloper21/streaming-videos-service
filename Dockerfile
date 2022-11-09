FROM tomcat:latest
ADD target/*.war /urs/local/tomcat/webapps/
EXPOSE 8083
CMD [ "catalina.sh", "run" ]