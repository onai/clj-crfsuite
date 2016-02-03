(ns clj-crfsuite.core
  (:require [clojure.walk :refer [stringify-keys]]))

;; need to do this again. classloaders change and
;; the existing stubs are orphaned.
(com.github.jcrfsuite.util.CrfSuiteLoader/load)

;; re-import libraries - now clojure and java should
;; see the same classes loaded by clojure's DynamicClassLoader
(import [com.github.jcrfsuite CrfTagger CrfTrainer]
        [com.github.jcrfsuite.util CrfSuiteLoader]
        [third_party.org.chokkan.crfsuite
         Attribute
         Item
         ItemSequence
         StringList])

(defn to-item
  "Allows you to specify an item in the following formats:
   1. {:feat1 :val1, :feat2 :val2}
   2. {:feat1 2.0, :feat2 3.0}
   3. {:feat1 true, :feat2 3.0}"
  [a-map]
  (let [str-map (stringify-keys a-map)
        an-item (Item.)]
    (doseq [[k v] str-map]
      (let [attr (cond (string? v)
                       (Attribute. (str k ":" v) 1.0)

                       (instance? Boolean v)
                       (Attribute. k (if v 1.0 0.0))

                       (keyword? v)
                       (Attribute. (str k ":" (name v)) 1.0)
                       
                       :else
                       (Attribute. k (double v)))]
        (.add an-item
              attr)))
    an-item))

(defn to-item-seq
  "A collection of maps is an itemsequence"
  [a-seq]
  (let [item-seq (ItemSequence.)]
    (doseq [a-map a-seq]
      (let [item (to-item a-map)]
        (.add item-seq
              item)))
    item-seq))

(defn to-string-list
  [a-str-vector]
  (let [s-list (StringList.)]
    (doseq [s a-str-vector]
      (.add s-list
            (name s)))
    s-list))

(defn train
  [x-seqs y-seqs model-file]
  (let [x-item-seqs (map to-item-seq x-seqs)
        y-str-seqs  (map to-string-list y-seqs)]
    (com.github.jcrfsuite.CrfTrainer/train x-item-seqs
                                           y-str-seqs
                                           model-file)))

(defrecord Tag [tag probability])

(defn tag
  [x-seq model-file]
  (let [tagger      (com.github.jcrfsuite.CrfTagger. model-file)
        x-item-seq (to-item-seq x-seq)]
    (map (fn [a-pair]
           (Tag. (.first a-pair)
                 (.second a-pair)))
         (.tag tagger x-item-seq))))
