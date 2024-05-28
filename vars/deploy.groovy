def deployWar(Map config) {
    def warFile = config.WAR_FILE
    def sshUsername = config.SSH_USERNAME
    def sshHostname = config.SSH_HOSTNAME
    def sshPort = config.SSH_PORT
    def remoteDirectory = config.REMOTE_DIRECTORY
    def remoteFilename = config.REMOTE_FILENAME

    if (!warFile) {
        error "WAR file not found!"
    }

    echo "Deploying WAR file: ${warFile}"

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        sh """
            scp -i ${SSH_KEY} -P ${sshPort} "${warFile}" "${sshUsername}@${sshHostname}:${remoteDirectory}/${remoteFilename}"
        """
    }
}


