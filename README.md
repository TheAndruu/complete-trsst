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

##### Create a story on a feed with ID 123:
    POST: http://localhost:8080/createStory/123
    {
      "title": "First post published",
      "datePublished": 1419989166384,
      "dateUpdated": 1419989166384
    }

##### Display a feed by a given id (example value 123):
    GET: http://localhost:8080/feed/123.atom

    <?xml version="1.0" encoding="UTF-8"?>
    <feed 
    xmlns="http://www.w3.org/2005/Atom">
    <title>Sample stories</title>
    <id>id: f402ca02-43c0-4809-9bbe-edecc9b9681f</id>
    <updated>2014-12-31T01:26:06Z</updated>
    <entry>
        <title>First post published</title>
        <id>f06a670f-6dde-4caf-b565-ceb74a752f58</id>
        <updated>2014-12-31T01:26:06Z</updated>
        <published>2014-12-31T01:26:06Z</published>
        <Signature 
            xmlns="http://www.w3.org/2000/09/xmldsig#">
            <SignedInfo>
                <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
                <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1" />
                <Reference URI="">
                    <Transforms>
                        <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
                    </Transforms>
                    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" />
                    <DigestValue>980yiUSW7UpB4YHliyRMt2QePlU=</DigestValue>
                </Reference>
            </SignedInfo>
            <SignatureValue>DB9T7a7MsoklPhmzXYxfljgtOteci78xNIOIAyiPY2PBHtSxWvXSC/v1Xlx86gko09niF5sIQJND
AL32LH0y2A==</SignatureValue>
            <KeyInfo>
                <KeyValue>
                    <ECKeyValue 
                        xmlns="http://www.w3.org/2009/xmldsig11#">
                        <NamedCurve 
                            xmlns:null="http://www.w3.org/2009/xmldsig11#" URI="urn:oid:1.3.132.0.10" />
                            <PublicKey>BN96EJppFA0la99oAfpFGjovday8x9OoHGbKqPQVudArVFudEikht/eeojv/VNKLg2b17dGxqXpw
tRHQ06yKmrc=</PublicKey>
                        </ECKeyValue>
                    </KeyValue>
                </KeyInfo>
            </Signature>
        </entry>
    </feed>





##### Display a feed by a given id (example value = 123) as HTML:
    GET: http://localhost:8080/feed/123.html


