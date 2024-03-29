pipeline{
    agent {label 'mac'}
    tools { maven 'mvn' }
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
		            		if [[ ! "$(docker ps -q --filter name=${JOB_NAME})" ]];
			        		then			            
			        			docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep ${JOB_NAME})
			        			echo "No container named ${JOB_NAME} found."	            		
			        		else
			        			containerId=$(docker ps -q --filter name=${JOB_NAME})
			        			containerImage=$(docker container inspect $(docker container ls -aq --filter id=$containerId) --format "{{.Image}}")
			        			if [[ $(docker container inspect -f '{{.State.Running}}' $containerId) == true ]];
			        			then
			        				docker stop $containerId
			        			fi
			        			docker rm --force $containerId
			        			docker rmi $containerImage
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
	            	fi
	            	
	            	hasEcrImage=$(aws ecr describe-images --repository-name "${AWS_ACCOUNT}/${JOB_NAME}" --region ${AWS_REGION} --image-ids=imageTag=latest > /dev/null 2>&1 && echo "yes" || echo "no")
	            	if [[ "$hasEcrImage" == "yes" ]];
	            	then
	            		aws ecr batch-delete-image --repository-name "${AWS_ACCOUNT}/${JOB_NAME}" --image-ids imageTag=latest
	            	fi
	            	
	            	docker tag ${JOB_NAME}:latest ${AWS_ECR_URI}/${AWS_ACCOUNT}/${JOB_NAME}:latest   
	            	docker push ${AWS_ECR_URI}/${AWS_ACCOUNT}/${JOB_NAME}:latest        	
	            '''
	        }
           
        }
        
        stage('Create ECS Cluster') {           
           steps {
               sh '''
	           		echo create cluster comming soon...
	            '''
           }
        }
    }
}
