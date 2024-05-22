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
        rsync -avz -e "ssh -p ${sshPort}" "${distDirectory}/*" "${sshUsername}@${sshHostname}:${remoteDirectory}/"

    }
}
