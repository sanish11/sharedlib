def getConfig() {
    def config = [
       
        SSH_USERNAME: 'Administrator',
        SSH_HOSTNAME: '103.94.159.179',
        SSH_PORT: '22',
         POM_FILE: 'kumari-backend/pom.xml',
        WAR_FILE: 'kumari-backend/kb-web/target/kb-web-0.0.1-SNAPSHOT.war',
    

    ]
    return config
}
