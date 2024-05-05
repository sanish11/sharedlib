def call(Map config) {
    def pomFile = config.POM_FILE
    def mavenHome = config.MAVEN_HOME

if (pomFile == null || !pomFile.isFile()) {
    error "Invalid POM file!"
}

    echo "Building project using Maven..."
    
    // Change to the directory containing the POM file and run Maven clean and package
    dir(pomFile.getParent()) {
    sh "${mavenHome}/bin/mvn clean package"
}

}
