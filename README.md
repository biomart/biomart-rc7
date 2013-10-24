This is an import from BioMart RC7 SVN repository.

This branch contains a new implementation of the frontend built with ruby,
coffeescript & jasmine, treated as a git submodule.

Building BioMart
----------------

Run ant:

    ant


Run Tests
---------

Run test ant file:

    ant -f build_test.xml test

Submodule
--------

Init submodule:

    git clone https://github.com/biomart/biomart-rc7
    git fetch
    git checkout -t origin/coffeescript
    git submodule init
    git submodule update

This will clone and checkout the github repo and initialize the submodule innto the
"frontend" directory.

