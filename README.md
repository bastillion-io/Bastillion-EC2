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

![Select Instances](https://freecode.com/screenshots/79/d3/79d33b7782be9573d4ffd5f6ed365663_medium.png)

![Terminals](https://freecode.com/screenshots/44/c4/44c4e44ff8e0be396bfe4d712e09109b_medium.png)

![More Terminals](https://freecode.com/screenshots/d0/1b/d01b5e2e1b513138af0ec4cdef522a36_medium.png)

![Upload Files](https://freecode.com/screenshots/5c/10/5c10df47dad8d43eb6ffeba0009d00db_medium.png)

![Disconnect](https://freecode.com/screenshots/41/5c/415cdd3ff23b76fb42d43db63c5ace70_medium.png)

Demo
-----
http://youtu.be/e00UD9dZ-wk

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

for Linux/Unix/OSX

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

for Windows

     set JAVA_HOME=C:\path\to\jdk
     set PATH=%JAVA_HOME%\bin;%PATH%

Start EC2Box

for Linux/Unix/OSX

        ./startEC2Box.sh

for Windows

        startEC2Box.bat

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

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by @ymnk
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by @chjj

Author
------
**Sean Kavanagh** 

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

(Follow me on twitter for release updates, but mostly nonsense)
