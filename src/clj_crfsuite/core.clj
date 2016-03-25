(ns clj-crfsuite.core
  (:require [clojure.walk :refer [stringify-keys]]))

;; this method loads the appropriate .sos etc.
;; ideally run on import but clojure 1.8 breaks
;; some classloader stuff this library depends on.
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

(defn to-item-map
  "Convert clojure maps to an ItemSequence"
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

(defn to-item-sequential
  "Convert clojure seqs to an ItemSequence."
  [a-seq]
  (let [the-map (into
                 {}
                 (map-indexed
                  (fn [i x]
                    [(-> i
                         str
                         keyword) x])
                  a-seq))]
    (to-item-map the-map)))

(defn to-item
  "Convert a feature-vector into an Item
  - interal CRFSuite type."
  [item]
  (cond (map? item)
        (to-item-map item)

        (sequential? item)
        (to-item-sequential item)))

(defn to-item-seq
  "Convert a clojure sequence to an ItemSequence.
  ItemSequence is a CRFSuite type - a collection
  of feature vectors"
  [a-seq]
  (let [item-seq (ItemSequence.)]
    (doseq [a-map a-seq]
      (let [item (to-item a-map)]
        (.add item-seq
              item)))
    item-seq))

(defn to-string-list
  "[String.] -> StringList.
  StringList. is an internal CRFSuite type"
  [a-str-vector]
  (let [s-list (StringList.)]
    (doseq [s a-str-vector]
      (.add s-list
            (name s)))
    s-list))

(defn train
  "Trains a crf model and saves it to disk.
  Args:
   x-seqs : A list of a sequence of feature-vectors.
   y-seqs : A list of sequence of tags
   model-file : Destination to write model to."
  [x-seqs y-seqs model-file]
  (let [x-item-seqs (map to-item-seq x-seqs)
        y-str-seqs  (map to-string-list y-seqs)]
    (com.github.jcrfsuite.CrfTrainer/train x-item-seqs
                                           y-str-seqs
                                           model-file)))

(defrecord Tag [tag probability])

(defn get-tagger
  "Loads a crf model from supplied location"
  [model-file]
  (com.github.jcrfsuite.CrfTagger. model-file))

(defn tag
  "Given a sequence of features, produce a sequence
  of tags.
  Args:
   x-seq : A sequence of feature-vectors
   tagger: A crf model
  Returns:
   A sequence of tags"
  [x-seq tagger]
  (let [x-item-seq (to-item-seq x-seq)]
    (map (fn [a-pair]
           (Tag. (.first a-pair)
                 (.second a-pair)))
         (.tag tagger x-item-seq))))
