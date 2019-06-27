FROM circleci/android:api-29

RUN sudo apt-get update && \
    sudo apt-get install --no-install-recommends -y openjdk-11-jre openjdk-11-jdk \
    && sudo rm -rf /var/lib/apt/lists/* 

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
RUN export JAVA_HOME
