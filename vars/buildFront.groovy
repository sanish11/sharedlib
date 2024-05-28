def call() {
    def nodejs = tool name: 'node16', type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'
    withEnv(["PATH+NODEJS=${nodejs}/bin"]) {
        dir('everest-frontend') {
            sh '''
                rm -rf node_modules
                echo "Installing npm packages"
                npm install
                echo "Running npm build"
                ng build
            '''
        }
    }
}
