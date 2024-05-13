// Change to the directory containing the POM file and run Maven clean and package
def call(Map config) {
    def pomFilePath = config.POM_FILE
    def mavenHome = config.MAVEN_HOME
    echo ":::: pomFilePath ::: ${pomFilePath}"

   

    echo "Building project using Maven..."

    // Execute Maven clean and package from the current working directory
    dir(pomFilePath){
        sh "${mavenHome}/bin/mvn clean install -X"

    }
}
