

    // Change to the directory containing the POM file and run Maven clean and package
   def call(Map config) {
    def pomFilePath = config.POM_FILE
    def mavenHome = config.MAVEN_HOME

    if (pomFilePath == null || !(new File(pomFilePath)).isFile()) {
        error "Invalid POM file!"
        echo "MAVEN_HOME: ${mavenHome}"
echo "Does POM file exist? ${new File(pomFilePath).exists()}"

    }

    echo "Building project using Maven..."

    // Execute Maven clean and package from the current working directory
    bat "${mavenHome}/bin/mvn -f ${pomFilePath} clean package"
}


