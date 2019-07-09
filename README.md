# EnsemblLinksetsCreator

## How to compile

### Note

This maven project is migrated from a previous version without maven.
The libraries existing in the previous one are installed into the local repository directly.
The pom.xml will be slowly adapted to load all the libraries from maven central.

### Compiling

1. Load the libraries into the local maven repository.

./loadlibraries.sh

2. Compile the code

mvn package

or to compile and run directly (eg.: from an IDE)

mvn exec:java -Dexec.args="/path/to/file.void /path/to/outputdirectory"

You can find the void file in https://www.bridgedb.org/data/linksets/archive/HomoSapiens/Ensembl_Hs_dataset.void.ttl
Alternatively you can use the script:

./loadvoid.sh

## How to run



