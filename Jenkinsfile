pipeline{
    agent any
    stages {
        stage('Build and Push to Artifactory') {
            steps {
                sh '''
                    export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.8.6/libexec
                    export PATH=$PATH:$MAVEN_HOME/bin
                    mvn --version
                    mvn clean install
                '''
            }
        }
        stage('Delete Docker container and previous Image version') {
        	steps {   
	            sh '''
	            	isImgExists=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME} >/dev/null 2>&1 && echo "yes" || echo "no")
	                if [[ "$isImgExists" -eq "yes" ]];
		            	then
		            		dockerImg=docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME}
		            		containerId=$(docker ps --all --quiet --filter ancestor=$dockerImg)
		            		if [-z "$containerId"];
			        		then			            
			        			docker rmi $dockerImg
			        			echo "No container found with image name $dockerImg"	            		
			        		else
			        			docker stop $containerId && docker rm $containerId
			        			docker rmi $dockerImg
			        		fi
			        	else
			        		echo "No ${JOB_NAME} image found."
			        	fi
	             '''
	        }
           
        }
        stage('Build and Publish image in Docker'){
            steps{
                sh '''
                    docker build --no-cache -t ${JOB_NAME}:${BUILD_NUMBER} .
                    docker images | grep ${JOB_NAME}
                '''
                echo "Build Process completed"
            }
        }
        stage('Create container in Docker'){
            steps{
     			sh '''
     				docker run -d -p 8083:8083 --name ${JOB_NAME} ${JOB_NAME}:${BUILD_NUMBER}
     				docker exec -ti ${JOB_NAME} sh -c "
     					mv webapps webapps2 && 
     					mv webapps.dist/ webapps && 
     					cp webapps2/StreamingVideoService.war webapps/StreamingVideoService.war && 
     					sed -i '/<\/tomcat-users>/ i\<role rolename="admin-gui"/>\n <role rolename="manager-gui"/>\n <user username="admin" password="Admin.12345" roles="manager-gui"/>' tomcat-users.xml
     					exit "
     				docker restart ${JOB_NAME}
     			'''
 			}              
        }
    }
}