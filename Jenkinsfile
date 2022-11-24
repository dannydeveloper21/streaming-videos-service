pipeline{
    agent {label 'mac'}
    tools { maven '3.8.6' }
    
    environment {
	     dockerhub=credentials('docker-hub')
	     contSts=''
	}
	
    stages {
    	stage('Check Maven version'){
    	   	steps {
	   	        sh '''
	   	        	mvn --version
	   	         '''
	   	    }                
    	                        
    	}
	    
	    stage('Unit test'){
	   	    steps {
            	sh "mvn clean test"
            }
	   	}

        stage('Build') {
            steps {
                sh '''
                    mvn clean install -Dmaven.test.skip=true
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
            steps {
     			script {
			         contSts= sh '''
				     			docker run -d -p 8083:8083 --name ${JOB_NAME} ${JOB_NAME}:${BUILD_NUMBER}
				     			contHostPort=$(docker port ${JOB_NAME} 8083/tcp)
				     			echo $(curl -s -o /dev/null -I -w '%{http_code}' http://$contHostPort/StreamingVideoService/actuator/health)
			     			'''
			     }
 			}              
        }
        
        stage('Push image to ECR') {
        	when {
	            expression {
            	   return contSts == 200
            	}
	        }
        	steps {
	            sh '''
	            	aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 583894140807.dkr.ecr.us-east-1.amazonaws.com
	            	docker tag ${JOB_NAME}:${BUILD_NUMBER} 583894140807.dkr.ecr.us-east-1.amazonaws.com/dannydeveloper2022/streaming-video-srv:latest	   
	            	docker push 583894140807.dkr.ecr.us-east-1.amazonaws.com/dannydeveloper2022/streaming-video-srv:latest         	
	            '''
	        }
           
        }
    }
}