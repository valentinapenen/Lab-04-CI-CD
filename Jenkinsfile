pipeline {
    agent any

    options {
        skipDefaultCheckout()
    }

    stages {
        stage('Descargar código') {
            steps {
                checkout scm
            }
        }

        stage('Compilar, probar y empaquetar') {
            steps {
                sh '''
                    sh ./mvnw -B clean package \
                        '-Dspring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1' \
                        '-Dspring.datasource.driver-class-name=org.h2.Driver' \
                        '-Dspring.datasource.username=sa' \
                        '-Dspring.datasource.password=' \
                        '-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect' \
                        '-Dspring.jpa.hibernate.ddl-auto=create-drop' \
                        '-Dspring.jpa.show-sql=false' \
                        '-Dapp.seed-admin=false'
                '''
            }
        }
        stage('Guardar JAR') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
