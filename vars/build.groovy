def call(Map config) {
    def pomFile = config.POM_FILE
    def mavenHome = config.MAVEN_HOME

    if (pomFile==null) {
        error "POM file not found!"
    }

    echo "Building project using Maven..."
    
    // Change to the directory containing the POM file and run Maven clean and package
    dir(pomFile.parent) {
        sh "${mavenHome}/bin/mvn clean package"
    }
}
