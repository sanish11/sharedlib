def call(Map config) {
    def pomFilePath = config.POM_FILE
    def mavenHome = config.MAVEN_HOME

    if (pomFilePath == null || !(new File(pomFilePath)).isFile()) {
        error "Invalid POM file!"
    }

    echo "Building project using Maven..."

    // Change to the directory containing the POM file and run Maven clean and package
    dir(new File(pomFilePath).getParent()) {
        sh "${mavenHome}/bin/mvn clean package"
    }
}
