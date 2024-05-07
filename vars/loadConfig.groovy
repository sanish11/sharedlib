def call() {
    def config = new ConfigSlurper().parse(new File("${libraryResource 'config.groovy'}").toURL())
    return config
}
