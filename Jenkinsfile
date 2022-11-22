pipeline{
    agent any
    
    environment {
	     dockerhub=credentials('docker-hub')
	}
	
    stages {
    	stage('Export Maven to PATH'){
    	   	steps {
	   	        sh '''
                    export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.8.6/libexec
                    export PATH=$PATH:$MAVEN_HOME/bin
                    mvn --version
	   	         '''
	   	    }                
    	                        
    	}
    	
    	stage('Clear') {
	       steps {
       	    sh "mvn clean"
       	   }
	    }
	    
	    stage('Tests') {
    	   parallel {
	   	        stage('Unit test'){
	   	            steps {
            	   	    sh "mvn test"
            	   	}
	   	        }
	   	        
	   	        stage('Integration test'){
	   	            steps {
            	   	    sh "mvn verify"
            	   	}
	   	        }
	   	    }
    	}

        stage('Build') {
            steps {
                sh '''
                    mvn install
                '''
            }
        }
        
        stage('Delete existing container and previous image version') {
        	steps {   
	            sh '''
	            	isImgExists=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME} >/dev/null 2>&1 && echo "yes" || echo "no")
	                if [[ "$isImgExists" -eq "yes" ]];
		            	then
		            		dockerImg=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME})
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
        
        stage('Build Docker image'){
            steps{
                sh '''
                    docker build --no-cache -t ${JOB_NAME}:${BUILD_NUMBER} .
                    docker images | grep ${JOB_NAME}
                '''
                echo "Build Process completed"
            }
        }
        
        stage('Create docker container'){
            steps{
     			sh '''
     				docker run -d -p 8083:8083 --name ${JOB_NAME} ${JOB_NAME}:${BUILD_NUMBER}
     			'''
 			}              
        }
        
        stage('Push image to Docker Hub') {
        	steps {
	            sh '''
	            	echo $docker_hub_PSW | docker login -u $docker-hub_USR --password-stdin
	            	
	            	img=$(docker images --quiet ${JOB_NAME}:${BUILD_NUMBER}) 
	            	docker tag $img developer2019/streaming-video-srv:${BUILD_NUMBER}
	            	docker push developer2019/streaming-video-srv:${BUILD_NUMBER}
	            '''
	        }
           
        }
    }
}