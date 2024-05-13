User
// Change to the directory containing the POM file and run Maven clean and package
def call(Map config) {
    def pomFilePath = config.POM_FILE
    def mavenHome = config.MAVEN_HOME
   // def javaHome = config.JAVA_HOME
    echo ":::: pomFilePath ::: ${pomFilePath}"
     def javaHomePath = tool name: 'jdk8', type: 'jdk'
    env.JAVA_HOME = javaHomePath

   

    echo "Building project using Maven..."

    // Execute Maven clean and package from the current working directory
    dir(pomFilePath) {
        // Specify the Java tool to be used
        tool name: 'jdk8', type: 'jdk'

        // Execute Maven clean and package using the specified Java version
        sh """
            export JAVA_HOME=${javaHome}
            ${mavenHome}/bin/mvn clean install -X
        """
    }
}
