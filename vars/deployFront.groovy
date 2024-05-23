def call(Map config) {
    def distDirectory = config.DIST_DIRECTORY
    def sshUsername = config.SSH_USERNAME
    def sshHostname = config.SSH_HOSTNAME
    def sshPort = config.SSH_PORT
    def remoteDirectory = config.REMOTE_DIRECTORY

    if (!fileExists(distDirectory)) {
        error "Dist directory not found: ${distDirectory}"
    }

    echo "Deploying dist directory: ${distDirectory}"

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        sh """
            rsync -avz -e 'ssh -i ${SSH_KEY} -p ${deployConfig.SSH_PORT}' ${deployConfig.DIST_DIRECTORY}/ ${deployConfig.SSH_USERNAME}@${deployConfig.SSH_HOSTNAME}:${deployConfig.REMOTE_DIRECTORY}/
        """
   
    }
}
