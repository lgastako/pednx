# pednx

EDN Transforms for the command line.

ednx is the spirtual predecessor targetting an npm installable node js command
line module whereas pednx is a port to a single file shebangable planck script.

## Prequisites

First, install planck:

```
$ brew install planck
```

(or similar)

## Usage

```
$ pednx assoc foo.edn :bar 5
$ cat foo.edn
{:bar 5}
$ pednx assoc-in foo.edn '[:baz :bif]' :bam
$ cat foo.edn
{:baz {:bif :bam}, :bar 5}
$ pednx remove foo.edn :bar
$ cat foo.edn
{:baz {:bif :bam}}
```

## TODO

- More examples
  - stdin/stdout
  - chains of pipes
- Parse command line as repeated reads instead of space separated reads, so we
  can read vectors, maps, etc.
  - Maybe quoting is fine?

## License

Copyright © 2014 John Evans.  All rights reserved (for now).
