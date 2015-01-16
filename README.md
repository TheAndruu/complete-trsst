complete-trsst
=============

Implementation of the Trsst protocol

System Requirements:
---------
Must have Java installed.

To run:
-------

###Linux / Mac OSX:

At a terminal from the project's root directory execute:
`./gradlew bootRun`

###Windows:
From the command prompt in the project's root directory, execute:
`gradlew.bat bootRun`

Brief Overview:
--------
Trsst syndicates digitally signed content with the highest grade cryptographic technology.

This provides message confidentiality across a number of dimensions:

- Authenticity - Knowing who created the message
- Integrity - Have messages been tampered or modified in any way
- Non-repudiation - The sender cannot deny creating a message



Examples:
---------

##### Creating signed posts programmatically

Digitally signing content with public key cryptography involves creating a public + private `KeyPair` to calculate the signature.

Trsst uses elliptic curve keys, which provide the highest form of security known today.

##### Create a keypair:

``` KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();``` 

##### Create a signed message:

Trsst operates on the Atom protocol.  Messages are Atom Entry nodes to Feed elements.  Both Feed and Entries are signed for independent verification.

    AtomSigner signer = new AtomSigner();
    String rawXml = signer.newEntry("Write your own message here!", keyPair);```

`rawXml` above will look like the contents of: <a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>.


##### Verify a signed message:

Signatures are only good if you can verify them, right?  To do so:

    AtomVerifier verifier = new AtomVerifier();
    boolean isFeedValid = verifier.isFeedVerified(domElement);
    boolean isEntryValid = verifier.areEntriesVerified(domElement);
    
The verification functions independently determine the signatures of the given feeds and entries.  

They return true if the signatures are valid, false if the content is modified, or `XMLSignatureException` if the signature is missing or broken.  For full use, see the JavaDocs.
    

##### Post a signed message

Messages are posted as Entries contained inside their respective Feed (both elements signed).  This allows the server to validate not only the content, but also authorize who may post to the given feed, since Feed IDs correspond with the keypair used to sign the content.

The above `AtomSigner.newEntry()` does this all behind the scenes and returns the results as raw XML.  Any alteration to this XML will invalidate the signature (which is desriable), including such things as whitespace and formatting, so be aware.

Making a post involves sending a signed Entry inside of a signed Feed element to /publish.  For example:

    POST: http://localhost:8080/publish
      
    Payload: 
    <Signed atom feed and entries>

    Response: 201 (Created) Stored onto feed: (feed id matching posted xml)
    -or-
    Response: 406 (Not acceptable) if a signature is not present or invalid
    
Example of a signed feed and entry can be found in:<a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>


##### Display a feed of signed entries by a given id (example value 123):
    GET: http://localhost:8080/feed/EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG

    Response: 200 (OK)
    Payload:
    <Signed atom feed and entries>
    
Again, for example of signed atom feed and entry, see <a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>.




