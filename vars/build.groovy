// Change to the directory containing the POM file and run Maven clean and package
def call(Map config) {
    
    def mavenHome = config.MAVEN_HOME
    echo ":::: pomFilePath ::: ${pomFilePath}"

   

    echo "Building project using Maven..."

   
        bat "\"${mavenHome}/bin/mvn\" clean package"
    
}
