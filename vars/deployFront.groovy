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

    sh "cd ${distDirectory} && zip -jr dist.zip . -i browser/*"

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        sh "scp -r -P ${sshPort} dist.zip ${sshUsername}@${sshHostname}:${remoteDirectory}"
        sh "ssh -p ${sshPort} ${sshUsername}@${sshHostname} \"powershell -Command Expand-Archive -Path '${remoteDirectory}/dist.zip' -DestinationPath '${remoteDirectory}'\""
    }
}
