(ns clji18n.test.core
  (:use clji18n.core)
  (:use [lazytest.describe :only (describe it testing given)]))

(def default-bundle {"name" "clji18n" nil nil})
(def en-bundle {"hi" "hi" "bye" "bye"})
(def es-bundle {"hi" "hola" "bye" "adiós"})
(def es-ar-bundle {"bye" "chau" "girl" "mina"})
(def es (locale "es"))
(def en (locale "en"))
(def es-ar (locale "es" "ar"))
(def tree (-> empty-tree
            (add-bundle default-bundle)
            (add-bundle en en-bundle)
            (add-bundle es es-bundle)
            (add-bundle es-ar es-ar-bundle)))

(describe "bundles"
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
      (= nil (resource tree es nil)))))

