# e2j

Tool for converting between EDN and JSON formats.  Work in progress.

## Build

Build, run through [ProGuard](http://proguard.sourceforge.net/), make a standalone executable jar:

    boot package
    
## Run

    target/e2j -h
    echo '{:x 1 :y [1 2 3]}' | target/e2j
