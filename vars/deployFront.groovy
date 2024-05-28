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
    
    sh "zip -r dist.zip ${distDirectory}"

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        sh "scp -P ${sshPort} dist.zip ${sshUsername}@${sshHostname}:${remoteDirectory}"
        sh "ssh -p ${sshPort} ${sshUsername}@${sshHostname} 'unzip -o ${remoteDirectory}/dist.zip -d ${remoteDirectory}/'"
    }
}
