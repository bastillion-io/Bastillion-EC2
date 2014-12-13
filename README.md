EC2Box
======
A web-based ssh console to execute commands and manage multiple EC2 instances
simultaneously running on Amazon Web Services (AWS). EC2Box allows you to share
terminal commands and upload files to all your EC2 instances. Once the sessions
have been opened you can select a single EC2 instance or any combination to run
your commands.  Also, additional instance administrators can be added and their
terminal sessions and history can be audited.  

![Terminals](http://ec2box.com/img/screenshots/medium/terms.png)

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

* Install [FreeOTP](https://fedorahosted.org/freeotp) or [Google Authenticator](https://github.com/google/google-authenticator) to enable two-factor authentication with Android or iOS

| FreeOTP       | Link                                                                                 |
|:------------- |:------------------------------------------------------------------------------------:|
| Android       | [Google Play](https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp)|
| iOS           | [iTunes](https://itunes.apple.com/us/app/freeotp/id872559395)                        |

| Google Authenticator| Link                                                                                               |
|:------------------- |:--------------------------------------------------------------------------------------------------:|
| Android             | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)|
| iOS                 | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605)                         |


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

Screenshots
-----------
![Importing Keys](http://ec2box.com/img/screenshots/medium/importing_keys.png)

![Importing Keys](http://ec2box.com/img/screenshots/medium/importing_keys.png)

![Select Instances](http://ec2box.com/img/screenshots/medium/select_instances.png)

![More Terminals](http://ec2box.com/img/screenshots/medium/more_terms.png)

![Upload Files](http://ec2box.com/img/screenshots/medium/upload_files.png)

![Disconnect](http://ec2box.com/img/screenshots/medium/disconnect.png)

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)

Author
------
**Sean Kavanagh** 

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

(Follow me on twitter for release updates, but mostly nonsense)
