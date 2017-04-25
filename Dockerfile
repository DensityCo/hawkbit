FROM ubuntu:latest
EXPOSE 8080

RUN apt-get -y update && apt-get -y install openjdk-8-jdk git curl vim maven
RUN git clone https://github.com/DensityCo/hawkbit.git /opt/hawkbit
WORKDIR /opt/hawkbit
RUN mvn install
CMD java -jar hawkbit-runtime/hawkbit-update-server/target/hawkbit-update-server-*-SNAPSHOT.jar