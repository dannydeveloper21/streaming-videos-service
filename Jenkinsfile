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
	                if ["$(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME} >/dev/null 2>&1 && echo -e 'yes' || echo -e 'no')" == "yes"];
		            then
		            dockerImg=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep ${JOB_NAME})
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
     				docker exec -it ${JOB_NAME} /bin/bash ; mv webapps webapps2; mv webapps.dist/ webapps; cp webapps2/StreamingVideoService.war webapps/StreamingVideoService.war; exit
     			'''
 			}              
        }
    }
}