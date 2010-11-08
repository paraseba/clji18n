(ns clji18n.core
  (:require [clojure.string :as s])
  (:import java.text.MessageFormat))

(defrecord Locale [language country variant])

(defn locale
  ([lang] (locale lang nil nil))
  ([lang country] (locale lang country nil))
  ([lang country variant] {:pre [(not (s/blank? lang))]}
   (Locale. lang country variant )))

(defprotocol ResourceMap
  (get-resource [b key])
  (has-key? [b key]))

(extend-type clojure.lang.IPersistentMap
  ResourceMap
  (get-resource [bundle key] (get bundle key))
  (has-key? [bundle key] (contains? bundle key)))

(def empty-tree {})

(def empty-locale (Locale. nil nil nil))

(defn add-bundle
  ([tree locale bundle] (assoc tree locale bundle))
  ([tree bundle] (assoc tree empty-locale bundle)))

(defn locale-seq [{:keys [language country variant]}]
  (distinct [(locale language country variant)
             (locale language country)
             (locale language)
             empty-locale]))

(defn bundle-seq [tree locale]
  (keep tree (locale-seq locale)))

(defn has-resource? [tree locale key]
  (boolean (some #(has-key? % key) (bundle-seq tree locale))))

(defn resource [tree locale key]
  (some
    #(and (has-key? % key) (get-resource % key))
    (bundle-seq tree locale)))

(defn java-locale [{:keys (language country variant)}]
  (java.util.Locale. language (or country "") (or variant "")))

(defn internationalize [tree locale key & args]
  (let [pattern (resource tree locale key)
        mf (doto (MessageFormat. "")
             ;for some reason it won't work creating the MessageFormat with the
             ;pattern, maybe I need to assign the locale before the pattern
             (.setLocale (java-locale locale))
             (.applyPattern pattern))]
    (.format mf (to-array args))))

(def *current-locale*)
(def *resource-tree*)

(defn _ [key & args]
  (apply internationalize *resource-tree* *current-locale* key args))

(defmacro with-locale [locale & body]
  `(binding [*current-locale* ~locale]
     ~@body))

(defmacro with-resources [tree & body]
  `(binding [*resource-tree* ~tree]
     ~@body))

(defn make-resource-tree [default-bundle & locale-tree-pairs]
  (let [tree (if default-bundle
               (add-bundle empty-tree default-bundle)
               empty-tree)]
    (reduce #(apply add-bundle %1 %2)
            tree
            (partition 2 locale-tree-pairs))))
