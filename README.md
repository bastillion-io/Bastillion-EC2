EC2Box
======

About
-----
A web-based ssh console to execute commands and manage multiple EC2 instances
simultaneously running on Amazon Web Services (AWS). EC2Box allows you to share
terminal commands and upload files to all your EC2 instances. Once the sessions
have been opened you can select a single EC2 instance or any combination to run
your commands.  Also, additional instance administrators can be added and their
terminal sessions and history can be audited.  

Screenshots
-----------

![Importing Keys](https://freecode.com/screenshots/94/c9/94c93beb0a5c47954514c55b7a0f90ea_medium.png)

![Select Instances](https://freecode.com/screenshots/87/0c/870c38a0716fda82c1297a882b55b55f_medium.png)

![Terminals](https://freecode.com/screenshots/7d/ad/7dad2b4771a9c2332746b4bfeeedd919_medium.png)

![More Terminals](https://freecode.com/screenshots/12/49/1249f116f0e6d4b4b97487e7c13289dd_medium.png)

![Upload Files](https://freecode.com/screenshots/5c/10/5c10df47dad8d43eb6ffeba0009d00db_medium.png)

Demo
-----
http://youtu.be/QcvMDjBb4SY

Prerequisites
-------------
Java JDK 1.7 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

Browser with Web Socket support
http://caniuse.com/websockets

**Note: In Safari if using a self-signed certificate you must import the certificate into your Keychain.
Select 'Show Certificate' -> 'Always Trust' when prompted in Safari

Maven 3 or greater  ( Only needed if building from source )
http://maven.apache.org

To Run Bundled with Jetty
------
If your not big on the idea of building from source...

Download ec2box-jetty-vXX.XX.tar.gz

https://github.com/skavanagh/EC2Box/releases

Export environment variables

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

Start EC2Box

        ./startEC2Box.sh

How to Configure SSL in Jetty
(it is a good idea to add or generate your own unique certificate)

http://wiki.eclipse.org/Jetty/Howto/Configure_SSL

To Build from Source 
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

**Note: Doing a mvn clean will delete the H2 DB and wipe out all the data.

Using EC2Box
------
Open browser to https://\<whatever ip\>:8443

Login with 

	username:admin 
	password:changeme

Steps:

1. Set your AWS credentials
2. Import the private key used on your EC2 systems (note: The EC2 server will only show if the private key has been imported)
3. Start composite-ssh sessions or create and execute a script across multiple sessions
4. Add instance administrator accounts
5. Audit sessions for instance administrators

Author
------
**Sean Kavanagh** 

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6


[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/0566eda40886c71548228fe00a8feed9 "githalytics.com")](http://githalytics.com/skavanagh/EC2Box)
