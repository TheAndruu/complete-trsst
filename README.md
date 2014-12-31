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
##### Display a feed by a given id (example value 123):
    GET: http://localhost:8080/feed/123.atom

##### Display a feed by a given id (example value = 123) as HTML:
    GET: http://localhost:8080/feed/123.html

##### Create a story on a feed with ID 123:
    POST: http://localhost:8080/createStory/123
    {
      "id": "asf",
      "title": "Title of story",
      "content": "content foo here",
      "contentType": "text/html",
      "datePublished": 1419989166384
    }

##### View story JSON for a given publisher ID (value = 123):
    GET: http://localhost:8080/viewPublisher/123

