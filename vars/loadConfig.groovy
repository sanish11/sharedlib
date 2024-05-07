def call() {
    def config = new ConfigSlurper().parse(new File("${libraryResource 'vars/config.groovy'}").toURL())
    return config
}
