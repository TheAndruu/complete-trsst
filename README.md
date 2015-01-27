complete-trsst
=============

Implementation of the Trsst protocol

System Requirements
---------
Must have Oracle Java 8 installed.

To see if it's already installed, at a new terminal, type: `java -version`.  You should see something like:

    java version "1.8.0_25"
    Java(TM) SE Runtime Environment (build 1.8.0_25-b17)

If so, you're good to go.  If it says "java isn't recognized" or mentions OpenJDK, mosey over to Oracle's site for the real deal.

To run
-------

### Linux / Mac OSX

To execute the development server, within a terminal from the project's root directory execute:
`./gradlew bootRun`

### Windows

To execute the development server, within the command prompt in the project's root directory, execute:
`gradlew.bat bootRun`

### Standalone server
Using `gradlew` as described above for your operating system, execute:
`./gradlew assemble`

The standalone server will then be created at:
`ct-webapp/build/libs/ct-webapp-1.0.war`

To run this server in a terminal, copy the war wherever desired and execute:
`java -jar ct-webapp-1.0.war`

### Deployed in a Tomcat

Build the `ct-webapp-1.0.war` file as described above.
Copy the built war file into TOMCAT_HOME/webapps
Start Tomcat using `./startup.sh`

Note: In this method, the URLs will reflect the name of the WAR file, for example, the 'search' URL would be: http://localhost:8080/ct-webapp-1.0/search

One way this can be altered is by renaming the war file prior to copying to 'webapps', such as to 'trsst.war' which would make the above URL: http://localhost:8080/trsst/search.  To see other ways this path can be configured, consult the Tomcat documentation.

Brief Overview
--------
Trsst syndicates digitally signed content with high grade cryptographic technology.

This provides message confidentiality across a number of dimensions:

- Authenticity - Knowing who created the message
- Integrity - Knowing if messages were tampered or modified in any way
- Non-repudiation - It cannot be denied that the message came from a given account

Trsst also enables encryption of message payloads using Public Key Cryptography. Since you own the keys, it's practically impossible for another party to read your private messages so long as they don't have your private key.

Examples:
---------

#### Programmatically creating signed content

Digitally signing content with public key cryptography involves creating a public + private `KeyPair` to calculate the signature.

Trsst uses elliptic curve keys, which provide the highest form of security known today.

#### Create a keypair

``` KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();``` 

#### Create a signed message

Trsst operates on the Atom protocol.  Messages are Atom Entry nodes to Feed elements.  Both Feed and Entries are signed for independent verification.

    AtomSigner signer = new AtomSigner();
    String rawXml = signer.newEntry("Write your own message here!", signKeys, encryptKey);

`rawXml` above will look like the contents of: <a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>.


#### Verify a signed message

Signatures are only good if you can verify them, right?  To do so:

    AtomVerifier verifier = new AtomVerifier();
    boolean isFeedValid = verifier.isFeedVerified(domElement);
    boolean isEntryValid = verifier.areEntriesVerified(domElement);
    
The verification functions independently determine the signatures of the given feeds and entries.  

They return true if the signatures are valid, false if the content is modified, or `XMLSignatureException` if the signature is missing or broken.  For full use, see the JavaDocs.

#### Create a signed, encrypted message

Trsst provides signed and encrypted private messages.  In addition to all the benefits of signed messages, these are virtually impossible to be decrypted by anyone not in possession of your private key, or the private keys of your recipients.

    AtomEncrypter encrypter = new AtomEncrypter();
    String signedEncryptedPost = encrypter.createEncryptedEntry("another new title", signKeys, encryptKeys, recipientPublicKeys);

`signedEncryptedPost` will contain the Atom xml, with the given title encrypted as the Content of the Atom Entry, and will only be decryptable by the `encrytKeys` keypair and the `recipientPublicKeys`

### Decrypt a signed, encrypted message

To decrypt an encrypted message, one must have either the signer's private key, or a private key of one of the recipients.  With these, a message may be decrypted by:

    EncryptionUtil util = new EncryptionUtil();
    String textContent = util.decryptText(signedEncryptedEntry, privateKey);

#### Post a message

Messages are posted as Entries contained inside their respective Feed (both elements signed).  This allows the server to validate not only the content, but also authorize who may post to the given feed, since Feed IDs correspond with the keypair used to sign the content.

The above `AtomSigner.newEntry()` does this all behind the scenes and returns the results as raw XML.  Any alteration to this XML will invalidate the signature (which is desriable), including such things as whitespace and formatting, so be aware.

Making a post involves sending a signed Entry inside of a signed Feed element to /publish.  For example:

    POST: http://localhost:8080/publish
      
    Payload: 
    <Signed atom feed and entries>

    Response: 201 (Created) Stored onto feed: (feed id matching posted xml)
    -or-
    Response: 406 (Not acceptable) if a signature is not present or invalid
    
Example of a signed feed and entry can be found in: <a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>


#### Display a feed
    GET: http://localhost:8080/feed/EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG

    Response: 200 (OK)
    Payload:
    <Signed atom feed and entries>
    
Again, for example of signed atom feed and entry, see <a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>.

#### Search feeds
    GET: http://localhost:8080/search/whatever search terms you like

    Response: 200 (OK)
    Payload:
    <Feed containing entries that match the given search terms>


#### Working example

There's a working example in `RestControllerIntegrationTest.java`

It demonstrates how to create a `KeyPair`, sign a message, and post it to the server.

To run, ensure the webapp is running (./gradlew bootRun) and run the unit test, either in your IDE of choice or in another terminal with ./gradlew clean test

Entries can be viewed in a browser at http://localhost:8080/feed/<feed public key value>

