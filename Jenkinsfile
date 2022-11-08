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
        stage('Delete Docker container ID and Image version by Image name') {
        	steps {
        		previousBuild = currentBuild.getPreviousBuild().result
        		println(currentBuild.getPreviousBuild())
				println(currentBuild.getPreviousBuild().result)
	            sh '''
	            	image=${JOB_NAME}:$previousBuild
	            	echo $image
	            	containerId=$(docker ps --all --quiet --filter ancestor=$image)
	            	
	            	if [! -z "$containerId"]; then
	            		docker stop $containerId && docker rm $containerId
	            		docker rmi $image
	            	else
	            		echo "No container found with image name $image"
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
     				docker run -d -p 8082:8082 ${JOB_NAME}:${BUILD_NUMBER}
     			'''
 			}              
        }
    }
}