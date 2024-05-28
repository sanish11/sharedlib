def call() {
    def nodejs = tool name: 'NodeJS 18.13.0', type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'
    withEnv(["PATH+NODEJS=${nodejs}/bin"]) {
        dir('customer-form-front') {
            sh '''
                rm -rf node_modules
                echo "Installing npm packages"
                npm install
                echo "Running npm build"
                npm run build
            '''
        }
    }
}
