// Change to the directory containing the POM file and run Maven clean and package
def call(Map config) {
    def pomFilePath = config.POM_FILE
    def mavenHome = config.MAVEN_HOME
    echo ":::: pomFilePath ::: ${pomFilePath}"

    echo "Building project using Maven..."

    // Execute Maven clean and package from the current working directory
    dir(pomFilePath) {
        // Specify the Java tool to be used
        tool name: 'jdk8', type: 'jdk'

        // Set JAVA_HOME environment variable
        def javaHomePath = tool name: 'jdk8', type: 'jdk'
        env.JAVA_HOME = javaHomePath

        // Execute Maven clean and package using the specified Java version
        sh """
            export JAVA_HOME=${env.JAVA_HOME}
            ${mavenHome}/bin/mvn clean install -X
        """
    }
}
