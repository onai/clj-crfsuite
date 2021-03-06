(ns clj-crfsuite.core
  (:require [clojure.walk :refer [stringify-keys]]
            [schema.core :as s]))

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

(s/defschema FeatureVector [s/Num])
(s/defschema FeatureMap {s/Any
                         (s/cond-pre s/Num s/Str s/Keyword s/Bool)})
(s/defschema FeatureSequence [(s/cond-pre FeatureVector FeatureMap)])

(s/defn to-item-map :- Item
  "Convert a feature map to an Item -
  an internal CRFSuite type"
  [a-map :- FeatureMap]
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

(s/defn to-item-sequential :- Item
  "Convert a feature vector to an Item"
  [a-seq :- FeatureVector]
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

(s/defn to-item-seq :- ItemSequence
  "Convert a clojure sequence to an ItemSequence.
  ItemSequence is a CRFSuite type - a collection
  of feature vectors"
  [a-seq :- FeatureSequence]
  (let [item-seq (ItemSequence.)]
    (doseq [a-map a-seq]
      (let [item (to-item a-map)]
        (.add item-seq
              item)))
    item-seq))

(s/defn to-string-list :- StringList
  "[String.] -> StringList.
  StringList. is an internal CRFSuite type"
  [a-str-vector :- [s/Str]]
  (let [s-list (StringList.)]
    (doseq [s a-str-vector]
      (.add s-list
            (name s)))
    s-list))

(s/defn train
  "Trains a crf model and saves it to disk.
  Args:
   x-seqs : A list of a sequence of feature-vectors.
   y-seqs : A list of sequence of tags
   model-file : Destination to write model to."
  [x-seqs :- FeatureSequence y-seqs model-file]
  (let [x-item-seqs (map to-item-seq x-seqs)
        y-str-seqs  (map to-string-list y-seqs)]
    (com.github.jcrfsuite.CrfTrainer/train x-item-seqs
                                           y-str-seqs
                                           model-file)))

(s/defrecord Tag [tag :- s/Str probability :- s/Num])

(defn get-tagger
  "Loads a crf model from supplied location"
  [model-file]
  (CrfTagger. model-file))

(s/defn tag :- [Tag]
  "Given a sequence of features, produce a sequence
  of tags.
  Args:
   x-seq : A sequence of feature-vectors
   tagger: A crf model
  Returns:
   A sequence of tags"
  [x-seq :- FeatureSequence
   tagger :- CrfTagger]
  
  (let [x-item-seq (to-item-seq x-seq)]
    (map (fn [a-pair]
           (Tag. (.first a-pair)
                 (.second a-pair)))
         (.tag tagger x-item-seq))))
