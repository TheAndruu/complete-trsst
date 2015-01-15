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

To play:
--------
Pages will be accessible in a browser at http://localhost:8080/, at least for GET requests.

For POST operations, consider using Postman until the client is delivered, a free Chrome browser app: <a href="https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm?hl=en">Postman in Chrome</a>.  

Examples:
---------

##### Make a post
Making a post involves sending a signed Entry inside of a signed Feed element to /publish.  For example:

    POST: http://localhost:8080/publish

    Payload: 
<feed xmlns="http://www.w3.org/2005/Atom"><id>urn:feed:EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG</id><updated>2015-01-15T00:53:50Z</updated><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/TR/1999/REC-xpath-19991116"><XPath xmlns:atom="http://www.w3.org/2005/Atom">//atom:feed[atom:id='urn:feed:EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG']</XPath></Transform><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>Z3t9W4lZEAZtY2GrmdgHQREh42Q=</DigestValue></Reference></SignedInfo><SignatureValue>27oyyWltP5buyurDDCO10xBAH76YqVBQZr7np4NcieZMmwfyAhuAPftY4W7xP+48e3G8LCFQp8uM
cmNaQhOtvg==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10"/><PublicKey>BN7ITSahM5Vq/cOupO9JxpfRPCRZDlEQYYwCVpGXSuQ7EhA0t4aWswA29khT4oleje0E6kYGNn3e
JK4mmXoCxrI=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature><entry xmlns="http://www.w3.org/2005/Atom"><title>hi everybody!</title><id>urn:uuid:cfa1ab4a-229c-44f1-9bb0-c49b25e032a1</id><updated>2015-01-15T00:53:50Z</updated><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/TR/1999/REC-xpath-19991116"><XPath xmlns:atom="http://www.w3.org/2005/Atom">//atom:entry[atom:id='urn:uuid:cfa1ab4a-229c-44f1-9bb0-c49b25e032a1']</XPath></Transform><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>DO2iFc+sKBuR0jfIYh7m96pH3wU=</DigestValue></Reference></SignedInfo><SignatureValue>q3Bt85qgLYgc7GnbLqBHAjDjxZlarUbtpkvnWr8CzQe3eBBUwm8ykCbukjrCJj9UjBlfRvbwCTAU
QJ3XOVFQIA==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10"/><PublicKey>BN7ITSahM5Vq/cOupO9JxpfRPCRZDlEQYYwCVpGXSuQ7EhA0t4aWswA29khT4oleje0E6kYGNn3e
JK4mmXoCxrI=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></entry></feed>

    Response: 201 (Created) Stored verified signed entry on feed: 123
    -or-
    Response: 406 (Not acceptable) if a signature is not present or invalid
Note: Since formatting matters in verifying XML signatures, if the above payload is having difficulty verifying when pasted from this readme, a workable example payload can be found in:<a href="https://github.com/TheAndruu/complete-trsst/blob/master/ct-core/src/test/resources/com/completetrsst/xml/feedValidEntryValid.xml">feedValidEntryValid.xml</a>


##### Display a feed of signed entries by a given id (example value 123):
    GET: http://localhost:8080/feed/EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG

    Response:
<feed xmlns="http://www.w3.org/2005/Atom"><id>urn:feed:EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG</id><updated>2015-01-15T00:53:50Z</updated><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/TR/1999/REC-xpath-19991116"><XPath xmlns:atom="http://www.w3.org/2005/Atom">//atom:feed[atom:id='urn:feed:EUvjLx5n9GWA1aMkvJ2GAvMAFeW1Av1HG']</XPath></Transform><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>Z3t9W4lZEAZtY2GrmdgHQREh42Q=</DigestValue></Reference></SignedInfo><SignatureValue>27oyyWltP5buyurDDCO10xBAH76YqVBQZr7np4NcieZMmwfyAhuAPftY4W7xP+48e3G8LCFQp8uM
cmNaQhOtvg==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10"/><PublicKey>BN7ITSahM5Vq/cOupO9JxpfRPCRZDlEQYYwCVpGXSuQ7EhA0t4aWswA29khT4oleje0E6kYGNn3e
JK4mmXoCxrI=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature><entry xmlns="http://www.w3.org/2005/Atom"><title>hi everybody!</title><id>urn:uuid:cfa1ab4a-229c-44f1-9bb0-c49b25e032a1</id><updated>2015-01-15T00:53:50Z</updated><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/TR/1999/REC-xpath-19991116"><XPath xmlns:atom="http://www.w3.org/2005/Atom">//atom:entry[atom:id='urn:uuid:cfa1ab4a-229c-44f1-9bb0-c49b25e032a1']</XPath></Transform><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>DO2iFc+sKBuR0jfIYh7m96pH3wU=</DigestValue></Reference></SignedInfo><SignatureValue>q3Bt85qgLYgc7GnbLqBHAjDjxZlarUbtpkvnWr8CzQe3eBBUwm8ykCbukjrCJj9UjBlfRvbwCTAU
QJ3XOVFQIA==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10"/><PublicKey>BN7ITSahM5Vq/cOupO9JxpfRPCRZDlEQYYwCVpGXSuQ7EhA0t4aWswA29khT4oleje0E6kYGNn3e
JK4mmXoCxrI=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></entry></feed>
    
    Response: 200 (OK)


