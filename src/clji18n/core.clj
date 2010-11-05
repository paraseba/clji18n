(ns clji18n.core
  (:require [clojure.string :as s]))

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

