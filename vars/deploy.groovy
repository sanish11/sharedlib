def deployWar(Map config) {
    def warFile = config.WAR_FILE
    def sshUsername = config.SSH_USERNAME
    def sshHostname = config.SSH_HOSTNAME
    def sshPort = config.SSH_PORT
    def remoteDirectory = config.REMOTE_DIRECTORY
    def remoteFilename = config.REMOTE_FILENAME

    if (warFile.empty) {
        error "WAR file not found!"
    }

    echo "Deploying WAR file: ${warFile}"

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        sh """
                            rsync -avz -e 'ssh -i ${env.SSH_KEY} -p ${env.SSH_PORT}' "${env.WAR_FILE}" "${env.SSH_USERNAME}@${env.SSH_HOSTNAME}:${env.REMOTE_DIRECTORY}/${env.REMOTE_FILENAME}"
                        """
    }
}
