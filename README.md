# e2j

Tool for converting between EDN and JSON formats.  Work in progress.

## Build

Build a standalone jar and run it through [ProGuard](http://proguard.sourceforge.net/):

    boot build package
    
## Run

    java -jar target/e2j-optimized.jar -h
    echo '{:x 1 :y [1 2 3]}' | java -jar target/e2j-optimized.jar
