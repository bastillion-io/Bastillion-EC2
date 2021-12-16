![Bastillion for EC2](https://www.bastillion.io/images/bastillion_40x40.png) 
Bastillion for EC2
======
A web-based ssh console to execute commands and manage multiple EC2 instances
simultaneously running on Amazon Web Services (AWS). Bastillion-EC2 allows you to share
terminal commands and upload files to all your EC2 instances. Once the sessions
have been opened you can select a single EC2 instance or any combination to run
your commands.  Also, additional instance administrators can be added and their
terminal sessions and history can be audited.  

![Terminals](https://www.bastillion.io/images/500x300/bastillion-ec2.png)


Bastillion for EC2 Releases
------
Bastillion-EC2 is available for free use under the Prosperity Public License

https://github.com/bastillion-io/Bastillion-EC2/releases

or purchase from the AWS marketplace

https://aws.amazon.com/marketplace/pp/Loophole-LLC-Bastillion-for-EC2/B076D7XMK6

Prerequisites
-------------
**Open-JDK / Oracle-JDK - 1.9 or greater**

*apt-get install openjdk-9-jdk*

> http://www.oracle.com/technetwork/java/javase/downloads/index.html

**Install [Authy](https://authy.com/) or [Google Authenticator](https://github.com/google/google-authenticator)** to enable two-factor authentication with Android or iOS

| Application          | Android                                                                                             | iOS                                                                        |             
|----------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| Authy                | [Google Play](https://play.google.com/store/apps/details?id=com.authy.authy)                        | [iTunes](https://itunes.apple.com/us/app/authy/id494168017)                |
| Google Authenticator | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

To Run Bundled with Jetty
------
Download bastillion-ec2-jetty-vXX.XX.tar.gz

https://github.com/bastillion-io/Bastillion-EC2/releases

Export environment variables

for Linux/Unix/OSX

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

for Windows

     set JAVA_HOME=C:\path\to\jdk
     set PATH=%JAVA_HOME%\bin;%PATH%

Start Bastillion

for Linux/Unix/OSX

        ./startBastillion-EC2.sh

for Windows

        startBastillion-EC2.bat
	
More documentation at: https://www.bastillion.io/docs/bastillion-ec2/index.html
	
	
Build from Source
------
Install Maven 3 or greater

*apt-get install maven*

> http://maven.apache.org 

Install Loophole MVC

> https://github.com/bastillion-io/lmvc

Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

*Note: Doing a mvn clean will delete the H2 DB and wipe out all the data.*

Using Bastillion-EC2
------
Open browser to https://\<whatever ip\>:8443

Login with 

	username:admin 
	password:changeme
	
*Note: When using the AMI instance, the password is defaulted to the \<Instance ID\>. Also, the AMI uses port 443 as in https://\<Instance IP\>:443*

Steps:

1. Set your AWS credentials for the following properties in the Bastillion-EC2.properties file. 
	```
	#AWS IAM access key
	accessKey=
	#AWS IAM secret key
	secretKey=
	```    
2. Configure an IAM Role with Account ID for your user and set generated ARN in Bastillion-EC2
3. Import the Bastillion-EC2 public SSH key to the EC2 AWS console.
4. Create EC2 instanaces with the imported key.
5. Start composite-ssh sessions or create and execute a script across multiple sessions
6. Add instance administrator accounts

More info at https://www.bastillion.io/docs/bastillion-ec2/index.html

Restricting User Access
------
Administrative access can be restricted through the use of tags defined in a user's profile. Profile tags must correspond to the instance tags that have been set through the AWS console.

Tags work on a name or name/value pair.

for example

    tag-name
    tag-name=mytag

or multiple

    tag-name1,tag-name2
    tag-name1=mytag1,tag-name2=mytag2
    
Supplying a Custom SSH Key Pair
------
Bastillion-EC2 generates its own public/private SSH key upon initial startup for use when registering systems.  You can specify a custom SSH key pair in the Bastillion-EC2Config.properties file.

For example:

	#set to true to regenerate and import SSH keys  --set to true
	resetApplicationSSHKey=true

	#SSH Key Type 'dsa' or 'rsa'
	sshKeyType=rsa

	#private key  --set pvt key
	privateKey=/Users/kavanagh/.ssh/id_rsa

	#public key  --set pub key
	publicKey=/Users/kavanagh/.ssh/id_rsa.pub
	
	#default passphrase  --leave blank if passphrase is empty
	defaultSSHPassphrase=myPa$$w0rd
	
After startup and once the key has been registered it can then be removed from the system. The passphrase and the key paths will be removed from the configuration file.

External Authentication
------
External Authentication can be enabled through the Bastillion-EC2Config.properties.

For example:

	#specify a external authentication module (ex: ldap-ol, ldap-ad).  Edit the jaas.conf to set connection details
	jaasModule=ldap-ol
    
Connection details need to be set in the jaas.conf file

    ldap-ol {
    	com.sun.security.auth.module.LdapLoginModule SUFFICIENT
    	userProvider="ldap://hostname:389/ou=example,dc=bastillion,dc=com"
    	userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    	authzIdentity="{cn}"
    	useSSL=false
    	debug=false;
    };
    

Administrators will be added as they are authenticated and profiles of systems may be assigned by full-privileged users.

User LDAP roles can be mapped to profiles defined in Bastillion-EC2 through the use of the org.eclipse.jetty.jaas.spi.LdapLoginModule.

    ldap-ol-with-roles {
        //openldap auth with roles that can map to profiles
        org.eclipse.jetty.jaas.spi.LdapLoginModule required
        debug="false"
        useLdaps="false"
        contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
        hostname="<SERVER>"
        port="389"
        bindDn="<BIND-DN>"
        bindPassword="<BIND-DN PASSWORD>"
        authenticationMethod="simple"
        forceBindingLogin="true"
        userBaseDn="ou=users,dc=bastillion,dc=com"
        userRdnAttribute="uid"
        userIdAttribute="uid"
        userPasswordAttribute="userPassword"
        userObjectClass="inetOrgPerson"
        roleBaseDn="ou=groups,dc=bastillion,dc=com"
        roleNameAttribute="cn"
        roleMemberAttribute="member"
        roleObjectClass="groupOfNames";
    };

Users will be added/removed from defined profiles as they login and when the role name matches the profile name.

Auditing
------
Auditing is disabled by default. Audit logs can be enabled through the **log4j2.xml** by uncommenting the **io.bastillion.manage.util.SystemAudit** and the **audit-appender** definitions.

> https://github.com/bastillion-io/Bastillion-EC2/blob/master/src/main/resources/log4j2.xml#L19-L22
	
Auditing through the application is only a proof of concept.  It can be enabled in the BastillionConfig.properties.

	#enable audit  --set to true to enable
	enableInternalAudit=true

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)

Third-party dependencies are mentioned in the [_3rdPartyLicenses.md_](3rdPartyLicenses.md)

The Prosperity Public License
-----------
Bastillion-EC2 is available for free use under the Prosperity Public License

Author
------
**Loophole, LLC - Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6
