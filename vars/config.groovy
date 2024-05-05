def getConfig() {
    def config = [
        TOMCAT_HOME: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0',
        CATALINA_HOME: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0',
        TOMCAT_WEBAPPS: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\webapps',
        SSH_USERNAME: 'Administrator',
        SSH_HOSTNAME: '103.94.159.179',
        SSH_PORT: '22',
        REMOTE_DIRECTORY: 'C:\\test',
        REMOTE_FILENAME: 'kumari.war',
      WAR_FILE: 'C:\\Program Files\\Jenkins\\Jenkins home\\workspace\\KUMARI\\kumari-backend\\kb-web\\target\\kb-web-0.0.1-SNAPSHOT.war', // Specify the path to your WAR file dynamically
                       
        VERBOSE: 'true'

    ]
    return config
}
