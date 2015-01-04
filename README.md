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
        <id>id: 5a78350d-63e9-4590-9cdf-1a21c44ab253</id>
        <updated>2014-12-31T01:26:06Z</updated>
        <entry 
            xmlns:sample="http://rome.dev.java.net/module/sample/1.0">
            <title>First post published</title>
            <id>08306b50-1db7-4725-ac2b-c8b51ea5e815</id>
            <updated>2014-12-31T01:26:06Z</updated>
            <published>2014-12-31T01:26:06Z</published>
            <Signature 
                xmlns="http://www.w3.org/2000/09/xmldsig#">
                <SignedInfo>
                    <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
                    <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#dsa-sha1" />
                    <Reference URI="">
                        <Transforms>
                            <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
                        </Transforms>
                        <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" />
                        <DigestValue>gk1EviFXm2p4Kz2o8Ye/XSo2tjc=</DigestValue>
                    </Reference>
                </SignedInfo>
                <SignatureValue>X5XIlSoj5wpO5YenqaKcN1sJAANiX14CGmbNAo5cV4wBPEuX3s4wrg==</SignatureValue>
                <KeyInfo>
                    <KeyValue>
                        <DSAKeyValue>
                            <P>/KaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII864WF64B81uRpH5t9jQTxeEu0ImbzRMqzVDZkVG9
    xD7nN1kuFw==</P>
                            <Q>li7dzDacuo67Jg7mtqEm2TRuOMU=</Q>
                            <G>Z4Rxsnqc9E7pGknFFH2xqaryRPBaQ01khpMdLRQnG541Awtx/XPaF5Bpsy4pNWMOHCBiNU0Nogps
    QW5QvnlMpA==</G>
                            <Y>A+ZcwSl6uEbQpjM1XibN1cigrZG7N4flJJQCDiHlgLmFSRvG2jnq6C8SEb6r6udH4t9zBu8H35ul
    ZBfwC/febw==</Y>
                        </DSAKeyValue>
                    </KeyValue>
                </KeyInfo>
            </Signature>
        </entry>
    </feed>



##### Display a feed by a given id (example value = 123) as HTML:
    GET: http://localhost:8080/feed/123.html


