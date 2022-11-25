pipeline{
    agent {label 'mac'}
    tools { maven '3.8.6' }
    parameters {
        booleanParam(name: 'FALSE_STS', defaultValue: false, description: '')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-east-2'], description: '')
        string(name: 'AWS_ACCOUNT', defaultValue: '583894140807', description: '')
        string(name: 'AWS_ECR_URI', defaultValue: '583894140807.dkr.ecr.us-east-1.amazonaws.com', description: '')
    }
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
	                if [[ "$isImgExists" == "yes" ]];
		            	then
		            		dockerImg=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME})
		            		containerId=$(docker ps --filter name=${JOB_NAME} >/dev/null 2>&1 && echo "yes" || echo "no")
		            		if [ "$containerId" == "no" ];
			        		then			            
			        			docker rmi $(docker images | grep ${JOB_NAME})
			        			echo "No container found with image name $dockerImg"	            		
			        		else
			        			docker stop $containerId && docker rm $containerId
			        			docker rmi $(docker images | grep ${JOB_NAME})
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
                	aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_URI}
	            	docker build --no-cache -t ${JOB_NAME} .
                    docker images | grep ${JOB_NAME}
                '''
                echo "Build Process completed"
            }
        }
        
        stage('Create docker container'){
            steps {
            	sh "docker run -d -p 8083:8083 --name ${JOB_NAME} ${JOB_NAME}"
			  
 			}              
        }
        
        stage('Push image to ECR') {
        	when {
	            expression {
	            	CONTAINER_STS = sh(returnStdout:true, script:'echo $(curl -s -o /dev/null -I -w "%{http_code}" http://$(docker port ${JOB_NAME} 8083/tcp)/StreamingVideoService/actuator/health)').trim()
            	    echo CONTAINER_STS
            	    return CONTAINER_STS == '200' || params.FALSE_STS
            	}
	        }
        	steps {
	            sh '''
	            	repExists=$(aws ecr describe-repositories --repository-names "${AWS_ACCOUNT}/${JOB_NAME}" --region ${AWS_REGION} --query "repositories[0].registryId" --output text >/dev/null 2>&1 && echo "yes" || echo "no")
	            	if [[ "$repExists" == "no" ]];
	            	then
	            		aws ecr create-repository --repository-name ${AWS_ACCOUNT}/${JOB_NAME} --region ${AWS_REGION}
	            		aws ecr describe-repositories --repository-names "${AWS_ACCOUNT}/${JOB_NAME}" --region ${AWS_REGION}
	            	fi
	            	docker tag ${JOB_NAME}:latest ${AWS_ECR_URI}/${AWS_ACCOUNT}/${JOB_NAME}:latest	   
	            	docker push ${AWS_ECR_URI}/${AWS_ACCOUNT}/${JOB_NAME}:latest         	
	            '''
	        }
           
        }
    }
}