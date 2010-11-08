;;  Copyright (c) Sebastián Galkin All rights reserved.  The use and
;;  distribution terms for this software are covered by the Eclipse Public
;;  License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;  be found in the file epl-v10.html at the root of this distribution.  By
;;  using this software in any fashion, you are agreeing to be bound by the
;;  terms of this license.  You must not remove this notice, or any other,
;;  from this software.
;;
;;  clji18n
;;
;;  Simple internationalization for Clojure
;;
;;  paraseba (gmail)
;;  Created 5 November 2010


(ns
  #^{:author "Sebastián Galkin"
     :doc "Simple internationalization for Clojure"}
  clji18n.core
  (:require [clojure.string :as s])
  (:import java.text.MessageFormat))

(defrecord Locale [language country variant])

(defn locale
  "Create a locale passing a language, optional country, and optional variant.
  Use two lowercase letters for the language, as defined by ISO-639.
  Use two uppercase letters for the country, asd defined by ISO-3166.
  Variant is vendor specific."
  ([lang] (locale lang nil nil))
  ([lang country] (locale lang country nil))
  ([lang country variant] {:pre [(not (s/blank? lang))]}
   (Locale. lang country variant )))

(defprotocol ResourceMap
  "A ResourceMap is a bundle composed of several resources identified by keys.
  Keys and resources could be of any type, according to what each implementation
  supports.
  IPersistentMap extends this protocol, so map is the default implementation."
  (get-resource [b key] "Get the resource identified by key or nil if not present")
  (has-key? [b key] "Return true if the resource identified by key is present in the ResourceMap"))

(extend-type clojure.lang.IPersistentMap
  ResourceMap
  (get-resource [bundle key] (get bundle key))
  (has-key? [bundle key] (contains? bundle key)))

(def
  #^{:doc "An empty tree of ResourceMaps"}
  empty-tree
  {})

(def #^{:private true} empty-locale (Locale. nil nil nil))

(defn add-bundle
  "Resource bundles form a tree or hierarchy with each bundle corresponding to a
  given locale. Bundles for specific locales, like in (locale \"es\" \"AR\" \"foo\"),
  may redifine resources declared in less specific locales, such as (locale \"es\").
  add-bundle is the mechanism to generate the bundles tree, starting with an
  empty-tree.
  If no locale is given, the bundle is used as a default, where all resources will
  be searched if not found elsewhere."
  ([tree locale bundle] (assoc tree locale bundle))
  ([tree bundle] (assoc tree empty-locale bundle)))

(defn- locale-seq [{:keys [language country variant]}]
  (distinct [(locale language country variant)
             (locale language country)
             (locale language)
             empty-locale]))

(defn- bundle-seq [tree locale]
  (keep tree (locale-seq locale)))

(defn has-resource?
  "Returns true if the resource identified by key if found in the given bundle tree
  for the given locale.
  The search is performed starting in the most specific locale (if present) and
  moving to less specific locales, until the default bundle is reached (if present)."
  [tree locale key]
  (boolean (some #(has-key? % key) (bundle-seq tree locale))))

(defn resource
  "Returns the resource identified by key if found in the given bundle tree
  for the given locale.
  The search is performed starting in the most specific locale (if present) and
  moving to less specific locales, until the default bundle is reached (if present)."
 [tree locale key]
  (some
    #(and (has-key? % key) (get-resource % key))
    (bundle-seq tree locale)))

(defn- java-locale [{:keys (language country variant)}]
  (java.util.Locale. language (or country "") (or variant "")))

(defn internationalize
  "Searches the resource identified by key in the given bundle tree and for the
  given locale. If found, it's internationalized using the resounce as a pattern
  for a MessageFormat instance, and with the given arguments.
  You can see examples of valid and useful patters in Java documentation for
  MessageFormat and ChoiceFormat:
  - http://download.oracle.com/javase/1.4.2/docs/api/java/text/MessageFormat.html
  - http://download.oracle.com/javase/1.4.2/docs/api/java/text/ChoiceFormat.html

  Sample: if the key :greeting returns a string resource of the form

     \"Hello {0}, it's {1,date,short}\"

  you could do

     (internationalize tree locale :greeting \"Rich\" (java.util.Date.))"
  [tree locale key & args]
  (let [pattern (resource tree locale key)
        mf (doto (MessageFormat. "")
             ;for some reason it won't work if I create the MessageFormat with the
             ;pattern, maybe I need to assign the locale before the pattern
             (.setLocale (java-locale locale))
             (.applyPattern pattern))]
    (.format mf (to-array args))))

(def
  #^{:doc "The current locale for the application. You can rebind this var"}
  *current-locale*)

(def
  #^{:doc "The full resource bundle tree for the application. You can rebind this var"}
  *resource-tree*)

(defn _
  "Internationalize the pattern identified by key with arguments args, using the
  locale *current-locale* and the bundle tree *resource-tree*.
  See also internationalize function."
  [key & args]
  (apply internationalize *resource-tree* *current-locale* key args))

(defmacro with-locale
  "Re bind *current-locale* var and evaluate body in that dynamic context."
  [locale & body]
  `(binding [*current-locale* ~locale]
     ~@body))

(defmacro with-resources
  "Re bind *resource-tree* var and evaluate body in that dynamic context."
  [tree & body]
  `(binding [*resource-tree* ~tree]
     ~@body))

(defn make-resource-tree
  "Helper for resource tree creation.
  locale-tree-pairs -> (locale1 bundle1 locale2 bundle2 ...)"
  [default-bundle & locale-tree-pairs]
  (let [tree (if default-bundle
               (add-bundle empty-tree default-bundle)
               empty-tree)]
    (reduce #(apply add-bundle %1 %2)
            tree
            (partition 2 locale-tree-pairs))))

