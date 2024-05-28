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
         sh "scp -r -P 22 dist.zip Administrator@103.94.159.179:C:/test"
         sh "ssh -p 22 Administrator@103.94.159.179 \"powershell -Command Expand-Archive -Path 'C:\\\\test\\\\dist.zip' -DestinationPath 'C:\\\\test\\\\'\""
    }
}
