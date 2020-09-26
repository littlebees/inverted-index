# Inverted Index for WARC

This is a HW of Informaiton Retrieval and Applications in NTUT.
In this HW, we need to generate the inverted index of WARC files using our own program.

And this HW is about inverted index instead of parsing.
Therefore, we allow to use an exising open source WARC parser.

The parser is from [Smerity/cc-warc-examples](https://github.com/Smerity/cc-warc-examples).

My inverted index program is in `org/commoncrawl/invertedindex`.
This program will generate two files, dictionary.txt and index.txt.

dictionary.txt contains all token in WARC.
index.txt is the inverted index.

## How to run
Use JDK8

Run `mvn clean compile exec:java`