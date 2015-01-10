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
Pages will be accessible in a browser at http://localhost:8080/

Examples:
---------

##### Post a signed atom element for storage under feed with ID 123:
    POST: http://localhost:8080/publish/123
    Content-type: application/xml
    Payload:
    <entry xmlns="http://www.w3.org/2005/Atom">
    <title>Title of story</title>
    <id>a397d6c5-8ffc-4f9c-967d-1979cf99e93a</id>
    <updated>2014-12-31T01:26:06Z</updated>
    <summary type="html">content foo here</summary>
<Signature xmlns="http://www.w3.org/2000/09xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" /><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1" /><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" /></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" /><DigestValue>PRWWcLWysjZK+iqUd7BNmyJ+v3k=</DigestValue></Reference></SignedInfo><SignatureValue>DgZ93jR2RlIOUBTf42GXDEp8pv/6m70SfrGmfZU4VUGpIiGpjvRo+2QJ0RjwUZtwpcwofbWZbopjPHnwlH6GhA==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10" /><PublicKey>BAkkrtMOHHqzSNrvwsCNDOW+e63frDGC2bGjqN1jcRLGb5KKUu30p6/FtrqW9hmso3e5ua195T4PnKSSEA8AscU=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></entry>

    Response: 201 (Created)

##### Display a feed of signed entries by a given id (example value 123):
    GET: http://localhost:8080/feed/123

    Response:
    <entry xmlns="http://www.w3.org/2005/Atom">
    <title>Title of story</title>
    <id>a397d6c5-8ffc-4f9c-967d-1979cf99e93a</id>
    <updated>2014-12-31T01:26:06Z</updated>
    <summary type="html">content foo here</summary>
    <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" /><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1" /><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" /></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" /><DigestValue>PRWWcLWysjZK+iqUd7BNmyJ+v3k=</DigestValue></Reference></SignedInfo><SignatureValue>DgZ93jR2RlIOUBTf42GXDEp8pv/6m70SfrGmfZU4VUGpIiGpjvRo+2QJ0RjwUZtwpcwofbWZbopjPHnwlH6GhA==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns="http://www.w3.org/2009/xmldsig11#"><NamedCurve xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10" /><PublicKey>BAkkrtMOHHqzSNrvwsCNDOW+e63frDGC2bGjqN1jcRLGb5KKUu30p6/FtrqW9hmso3e5ua195T4PnKSSEA8AscU=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></entry>

    Response: 200 (OK)


