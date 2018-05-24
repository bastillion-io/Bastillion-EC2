EC2Box
======
A web-based ssh console to execute commands and manage multiple EC2 instances
simultaneously running on Amazon Web Services (AWS). EC2Box allows you to share
terminal commands and upload files to all your EC2 instances. Once the sessions
have been opened you can select a single EC2 instance or any combination to run
your commands.  Also, additional instance administrators can be added and their
terminal sessions and history can be audited.  

![Terminals](https://www.sshkeybox.com/images/500x300/ec2box.png)

AMI in the AWS Marketplace
------
The simplest way to get started is to run the AMI from the AWS marketplace.

http://aws.amazon.com/marketplace/pp/B076D7XMK6

Once the EC2Box instance is up and running, open your browser to https://\<EC2 Instance IP\>:443

Login with 

	username:admin 
	password:<Instance ID>

EC2Box Releases
------
EC2Box releases with Jetty are no longer available on Github. Releases and upgrades are available via subscription through the following link: 

https://www.sshkeybox.com/subscription

Feel free to try out EC2Box using the build instructions below!

Prerequisites
-------------
**Open-JDK / Oracle-JDK - 1.9 or greater**

> apt-get install openjdk-9-jdk

or

> http://www.oracle.com/technetwork/java/javase/downloads/index.html

**Browser with Web Socket support**
http://caniuse.com/websockets  *(Note: In Safari if using a self-signed certificate you must import the certificate into your Keychain.
Select 'Show Certificate' -> 'Always Trust' when prompted in Safari)*

**Maven 3 or greater**  *(Only needed if building from source)*

> apt-get install maven

or 

> http://maven.apache.org 

**Loophole MVC**  *(Only needed if building from source)*

> https://github.com/skavanagh/lmvc

**Install [FreeOTP](https://freeotp.github.io/) or [Google Authenticator](https://github.com/google/google-authenticator)** to enable two-factor authentication with Android or iOS

| Application          | Android                                                                                             | iOS                                                                        |             
|----------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| FreeOTP              | [Google Play](https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp)               | [iTunes](https://itunes.apple.com/us/app/freeotp/id872559395)              |
| Google Authenticator | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

Build from Source
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

*Note: Doing a mvn clean will delete the H2 DB and wipe out all the data.*

Using EC2Box
------
Open browser to https://\<whatever ip\>:8443

Login with 

	username:admin 
	password:changeme
	
*Note: When using the AMI instance, the password is defaulted to the \<Instance ID\>. Also, the AMI uses port 443 as in https://\<Instance IP\>:443*

Steps:

1. Set your AWS credentials
2. Import the private key used on your EC2 systems *(Note: The EC2 server will only show if the private key has been imported)*
3. Start composite-ssh sessions or create and execute a script across multiple sessions
4. Add instance administrator accounts
5. Audit sessions for instance administrators

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

External Authentication
------
External Authentication can be enabled through the EC2BoxConfig.properties.

For example:

	#specify a external authentication module (ex: ldap-ol, ldap-ad).  Edit the jaas.conf to set connection details
	jaasModule=ldap-ol
    
Connection details need to be set in the jaas.conf file

    ldap-ol {
    	com.sun.security.auth.module.LdapLoginModule SUFFICIENT
    	userProvider="ldap://hostname:389/ou=example,dc=ec2box,dc=com"
    	userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    	authzIdentity="{cn}"
    	useSSL=false
    	debug=false;
    };
    

Administrators will be added as they are authenticated and profiles of systems may be assigned by full-privileged users.

User LDAP roles can be mapped to profiles defined in EC2Box through the use of the org.eclipse.jetty.jaas.spi.LdapLoginModule.

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
        userBaseDn="ou=users,dc=ec2box,dc=com"
        userRdnAttribute="uid"
        userIdAttribute="uid"
        userPasswordAttribute="userPassword"
        userObjectClass="inetOrgPerson"
        roleBaseDn="ou=groups,dc=ec2box,dc=com"
        roleNameAttribute="cn"
        roleMemberAttribute="member"
        roleObjectClass="groupOfNames";
    };

Users will be added/removed from defined profiles as they login and when the role name matches the profile name.

Screenshots
-----------
![Two-Factor](https://www.sshkeybox.com/images/screenshots/medium/ec2box/two-factor.png)

![Importing Keys](https://www.sshkeybox.com/images/screenshots/medium/ec2box/importing_keys.png)

![Select Instances](https://www.sshkeybox.com/images/screenshots/medium/ec2box/select_instances.png)

![More Terminals](https://www.sshkeybox.com/images/screenshots/medium/ec2box/more_terms.png)

![Upload Files](https://www.sshkeybox.com/images/screenshots/medium/ec2box/upload_files.png)

![Disconnect](https://www.sshkeybox.com/images/screenshots/medium/ec2box/disconnect.png)

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)

Third-party dependencies are mentioned in the [_3rdPartyLicenses.md_](3rdPartyLicenses.md)

Dual License
-----------
EC2Box is available for non-commercial use under the Affero General Public License

A commercial license is also available through a subscription

https://www.sshkeybox.com/subscription

or when running an AMI from the AWS marketplace.

http://aws.amazon.com/marketplace/pp/B076PNFPCL

Author
------
**Loophole, LLC - Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

(Follow me on twitter for release updates, but mostly nonsense)
