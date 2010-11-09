# clji18n

Simple internationalization for Clojure

## Usage

Create maps with internationalized resources (e.g. strings)

<pre><code>
(def en-resources {:hi "hello {0}", "bye" "bye"})
(def es-resources {:hi "hola {0}", "bye" "adiós"})
(def es-ar-resources {"bye" "chau"})
</code></pre>

Keys and values could be anything, but it's probably useful to use keywords
or strings as keys.

Create a tree of resource bundles

<pre><code>
(def tree (make-resource-tree en-resources
                              (locale "es") es-resources)
                              (locale "es" "AR") es-ar-resources))
</code></pre>

Get your internationalized resources

<pre><code>
(with-locale (locale "es" "AR")
  (with-resources tree
    (println (_ :hi "Rich") (_ "bye"))))
</code></pre>

That would print "hola Rich" and "chau". :hi key is get from es-ar-resources bundle,
"bye" key is get from the parent bundle es-resources.

There is and will be more to clji18n. To be documented...


## Installation

Add [clji18n "0.1.0"] to your project.clj

## License

Copyright (C) 2010 Sebastián Galkin

Distributed under the Eclipse Public License, the same as Clojure.
