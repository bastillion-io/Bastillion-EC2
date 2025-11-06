![Build](https://github.com/bastillion-io/Bastillion-EC2/actions/workflows/github-build.yml/badge.svg)
![CodeQL](https://github.com/bastillion-io/Bastillion-EC2/actions/workflows/codeql-analysis.yml/badge.svg)

![Bastillion for EC2](https://www.bastillion.io/images/bastillion_40x40.png)

# Bastillion for EC2

**A modern, web-based SSH console and key management tool for Amazon EC2.**

Bastillion for EC2 provides a browser-based SSH management platform designed specifically for AWS environments.  
It enables secure access, auditing, and centralized key management across all your EC2 instances—built on the same foundation as Bastillion and now updated for **Java 21 / Jakarta EE 11**.

![Terminals](https://www.bastillion.io/images/500x300/bastillion-ec2.png)

---

## 🚀 What’s New
- Upgraded to **Java 21** and **Jakarta EE 11**
- Full support for **Ed25519** (default) and **Ed448** SSH keys
- New **daemon mode** for Jetty startup (`--daemon`)
- Updated dependencies for improved security and performance
- Clarified AWS IAM and EC2 integration steps

---

## Installation Options
**Free:** https://github.com/bastillion-io/Bastillion-EC2/releases  
**AWS Marketplace:** https://aws.amazon.com/marketplace/pp/Loophole-LLC-Bastillion-for-EC2/B076D7XMK6

---

## Prerequisites

### Java 21 (OpenJDK or Oracle JDK)
```bash
apt-get install openjdk-21-jdk
```
> Oracle JDK download: http://www.oracle.com/technetwork/java/javase/downloads/index.html

### Authenticator (for 2FA)

| Application | Android | iOS |
|--------------|----------|-----|
| **Authy** | [Google Play](https://play.google.com/store/apps/details?id=com.authy.authy) | [iTunes](https://itunes.apple.com/us/app/authy/id494168017) |
| **Google Authenticator** | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

---

## Run with Jetty (Bundled)

Download: https://github.com/bastillion-io/Bastillion-EC2/releases

### Set Environment Variables
**Linux / macOS**
```bash
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH
```
**Windows**
```cmd
set JAVA_HOME=C:\path\to\jdk
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Start Bastillion for EC2
Foreground (interactive):
```bash
./startBastillion-EC2.sh
```

Daemon (background):
```bash
./startBastillion-EC2.sh --daemon
```
Logs are stored in `jetty/logs/YYYY_MM_DD.jetty.log`.

Enable debug output:
```bash
./startBastillion-EC2.sh -d
```

Stop:
```bash
./stopBastillion-EC2.sh
```

Access in browser:  
`https://<server-ip>:8443` (or for AMI instances: `https://<instance-ip>:443`)

Default credentials:
```
username: admin
password: changeme
```
*(For AMI, the password defaults to the EC2 Instance ID.)*

---

## AWS Integration Steps
1. Configure an **IAM Role** with your Account ID and set the generated ARN in Bastillion-EC2.
2. Import the **Bastillion-EC2 public SSH key** into the AWS EC2 console.
3. Launch EC2 instances using that key pair.
4. Start composite SSH sessions or run scripts across multiple instances.
5. Add instance administrator accounts as needed.

More info: https://www.bastillion.io/docs/bastillion-ec2/index.html

---

## Restricting User Access
Administrative access can be restricted through tags defined in a user’s profile.  
Profile tags must match the EC2 instance tags set in AWS.

Examples:
```
tag-name
tag-name=mytag
tag1=value1,tag2=value2
```

---

## Custom SSH Key Pair

Specify a custom SSH key pair or let Bastillion E2 generate its own on startup:

```properties
# Regenerate and import SSH keys
resetApplicationSSHKey=true

# SSH key type ('rsa', 'ecdsa', 'ed25519', or 'ed448')
# Supported options:
#   rsa    - Classic, widely compatible (configurable length, default 4096)
#   ecdsa  - Faster, smaller keys (P-256/384/521 curves)
#   ed25519 - Default and recommended (≈ RSA-4096, secure and fast)
#   ed448  - Extra-strong (≈ RSA-8192, slower and less supported)
sshKeyType=ed25519

# Private key
privateKey=/Users/you/.ssh/id_rsa

# Public key
publicKey=/Users/you/.ssh/id_rsa.pub

# Passphrase (leave blank if none)
defaultSSHPassphrase=myPa$$w0rd
```

Once registered, you can remove the key files and passphrase from the configuration.

---

## External Authentication (LDAP / AD)
Enable in `Bastillion-EC2Config.properties`:
```properties
jaasModule=ldap-ol
```

Configure `jaas.conf`:
```
ldap-ol {
    com.sun.security.auth.module.LdapLoginModule SUFFICIENT
    userProvider="ldap://hostname:389/ou=example,dc=bastillion,dc=com"
    userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    authzIdentity="{cn}"
    useSSL=false
    debug=false;
};
```

Map LDAP roles to Bastillion profiles:
```
ldap-ol-with-roles {
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
```

Users are added/removed as they authenticate when their role matches a Bastillion profile.

---

## Auditing
Auditing is disabled by default.  
Enable in **log4j2.xml** by uncommenting:
- `io.bastillion.manage.util.SystemAudit`
- `audit-appender`

> https://github.com/bastillion-io/Bastillion-EC2/blob/master/src/main/resources/log4j2.xml#L19-L22

Then set in `Bastillion-EC2Config.properties`:
```properties
enableInternalAudit=true
```

---

## Acknowledgments
Special thanks to these projects that make Bastillion possible:

- [JSch](http://www.jcraft.com/jsch) (Java Secure Channel) by [ymnk](https://github.com/ymnk)
- [term.js](https://github.com/chjj/term.js) (A JavaScript terminal) by [chjj](https://github.com/chjj)

Third-party dependencies are listed in [_3rdPartyLicenses.md_](3rdPartyLicenses.md)

---

## License
Bastillion-EC2 is distributed under the **Prosperity Public License**.

---

## Author

**Loophole, LLC — Sean Kavanagh**  
Email: [sean.p.kavanagh6@gmail.com](mailto:sean.p.kavanagh6@gmail.com)  
Instagram: [@spkavanagh6](https://www.instagram.com/spkavanagh6/)