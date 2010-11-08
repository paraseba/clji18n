(ns clji18n.test.core
  (:import (java.util Calendar GregorianCalendar Date))
  (:use clji18n.core)
  (:use [lazytest.describe :only (describe it testing given)]))


(describe "bundles"
  (given [default-bundle {"name" "clji18n" nil nil}
          en-bundle {"hi" "hi" "bye" "bye"}
          es-bundle {"hi" "hola" "bye" "adiós"}
          es-ar-bundle {"bye" "chau" "girl" "mina"}
          es (locale "es")
          en (locale "en")
          es-ar (locale "es" "ar")
          tree (-> empty-tree
                 (add-bundle default-bundle)
                 (add-bundle en en-bundle)
                 (add-bundle es es-bundle)
                 (add-bundle es-ar es-ar-bundle))]
    (testing has-resource?
      (it "returns false for unknown resources"
        (= false (has-resource? tree es-ar "what?")))
      (it "returns true for resources in the locale bundle"
        (= true (has-resource? tree es-ar "bye")))
      (it "returns true for resources in the parent locale bundle"
        (= true (has-resource? tree es-ar "hi")))
      (it "returns true for resources in the default bundle"
        (= true (has-resource? tree es-ar "name")))
      (it "returns false for resources in a child bundle"
        (= false (has-resource? tree es "girl")))
      (it "detects nil keys and values"
        (= true (has-resource? tree es nil))))

    (testing resource
      (it "returns nil for unknown resource"
        (nil? (resource tree es-ar "what?")))
      (it "returns value for resources in the locale bundle"
        (= "chau" (resource tree es-ar "bye")))
      (it "returns value for resources in the parent locale bundle"
        (= "hola" (resource tree es-ar "hi")))
      (it "returns value for resources in the default bundle"
        (= "clji18n" (resource tree es-ar "name")))
      (it "returns nil for resources in a child bundle"
        (nil? (resource tree es "girl")))
      (it "returns nil for nil values"
        (= nil (resource tree es nil))))))

(describe interationalize
  (given [en-bundle {"bye" "bye"
                     :the-girl-phrase "{0} was born on {1,date,short}"
                     :plural "{0,choice,0#no Clojure programmers|1#a single Clojure programmer|1<{0,number,integer} Clojure programmers}"}
          es-ar-bundle {"bye" "chau"
                        :the-girl-phrase "{0} nació el {1,date,short}"
                        :plural "{0,choice,0#ningún programador Clojure|1#solo un programador Clojure|1<{0,number,integer} programadores Clojure}"}
          en-us (locale "en" "US")
          es-ar (locale "es" "AR")
          tree (-> empty-tree
                 (add-bundle en-us en-bundle)
                 (add-bundle es-ar es-ar-bundle))]

    (testing "simple messages"
      (it "formats messages without arguments in es"
        (= "chau" (internationalize tree es-ar "bye")))
      (it "formats messages without arguments in en"
        (= "bye" (internationalize tree en-us "bye"))))

    (testing "messages with parameters"
      (it "formats messages with 2 arguments in es"
        (= "María nació el 05/01/00" (internationalize tree es-ar
                                                  :the-girl-phrase
                                                  "María"
                                                  (.getTime (GregorianCalendar. 2000 Calendar/JANUARY 5)))))

      (it "formats messages with 2 arguments in en"
        (= "Maria was born on 1/5/00" (internationalize tree en-us
                                                  :the-girl-phrase
                                                  "Maria"
                                                  (.getTime (GregorianCalendar. 2000 Calendar/JANUARY 5))))))

    (testing "choice formatters"
      (testing "in english"
        (it "formats 0"
          (= "no Clojure programmers" (internationalize tree en-us :plural 0)))
        (it "formats singular"
          (= "a single Clojure programmer" (internationalize tree en-us :plural 1)))
        (it "formats plurals"
          (= "2 Clojure programmers" (internationalize tree en-us :plural 2))))

      (testing "in spanish"
        (it "formats 0"
          (= "ningún programador Clojure" (internationalize tree es-ar :plural 0)))
        (it "formats singular"
          (= "solo un programador Clojure" (internationalize tree es-ar :plural 1)))
        (it "formats plurals"
          (= "2 programadores Clojure" (internationalize tree es-ar :plural 2)))))))

(describe _
  (given [en-bundle {:hi "hi {0}"}
          es-bundle {:hi "hola {0}"}
          tree (-> empty-tree
                 (add-bundle (locale "en") en-bundle)
                 (add-bundle (locale "es") es-bundle))]
    (it "gets english translation"
      (with-locale (locale "en")
        (with-resources tree
          (= "hi Rich" (_ :hi "Rich")))))
    (it "gets spanish translation"
      (with-locale (locale "es")
        (with-resources tree
          (= "hola Rich" (_ :hi "Rich")))))))
