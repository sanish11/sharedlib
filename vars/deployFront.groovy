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

    // Navigate to the browser directory and zip its contents
    dir(distDirectory) {
        sh "zip -r dist.zip ."
    }

    withCredentials([sshUserPrivateKey(credentialsId: 'private', keyFileVariable: 'SSH_KEY')]) {
        // Remove all files in the remote directory before copying the new zip
        sh """
            ssh -p ${sshPort} ${sshUsername}@${sshHostname} \"
                if (Test-Path -Path '${remoteDirectory}') {
                    Remove-Item -Path '${remoteDirectory}\\*' -Recurse -Force
                }
            \"
        """
        
        // SCP the dist.zip to the remote server
        sh "scp -P ${sshPort} ${distDirectory}/dist.zip ${sshUsername}@${sshHostname}:${remoteDirectory}"
        
        // Unzip the new dist.zip on the remote server
        sh """
            ssh -p ${sshPort} ${sshUsername}@${sshHostname} \"
                powershell -Command Expand-Archive -Path '${remoteDirectory}\\dist.zip' -DestinationPath '${remoteDirectory}'
            \"
        """
    }
}
